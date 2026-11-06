"""Bundle the reobfuscated CE QoL and pinned upstream JER into one Forge 1.12.2 JAR."""

import argparse
import hashlib
import json
from pathlib import Path
from tempfile import NamedTemporaryFile
from zipfile import ZIP_DEFLATED, ZipFile, ZipInfo


JER_SHA256 = "9ea4d7df19c0a53880aae5e745ef1e886854bea5c533878688587a5e3fdaebdd"
MANIFEST = "META-INF/MANIFEST.MF"
METADATA = "mcmod.info"
JER_AT = "jeresources_at.cfg"
JER_IGNORED = {
    MANIFEST, METADATA, "pack.mcmeta",
    "META-INF/fml_cache_annotation.json", "META-INF/fml_cache_class_versions.json",
}


def read_entries(path):
    with ZipFile(path) as jar:
        entries = {}
        for info in jar.infolist():
            if info.is_dir():
                continue
            if info.filename in entries:
                raise ValueError(f"Duplicate entry in {path.name}: {info.filename}")
            entries[info.filename] = jar.read(info)
        return entries


def bundle(base, upstream, output):
    if output.resolve() in {base.resolve(), upstream.resolve()}:
        raise ValueError("Choose a separate output path for the bundled JAR.")
    if hashlib.sha256(upstream.read_bytes()).hexdigest() != JER_SHA256:
        raise ValueError("Expected the original JER 1.12.2-0.9.2.60 release JAR (CurseForge file 2728585).")
    ceqol = read_entries(base)
    jer = read_entries(upstream)
    metadata = json.loads(ceqol[METADATA])
    if [mod["modid"] for mod in metadata] != ["ceqol"]:
        raise ValueError("Expected an unbundled CE QoL base JAR.")
    manifest = ceqol[MANIFEST].rstrip(b"\r\n")
    if b"FMLAT:" in manifest or b"FMLCorePluginContainsFMLMod: true" not in manifest:
        raise ValueError("Unexpected CE QoL manifest. Check the core plugin and access transformers.")
    if b"\r\n\r\n" in manifest:
        raise ValueError("Expected only the main section of the CE QoL manifest.")
    if f"FMLAT: {JER_AT}".encode() not in jer[MANIFEST] or f"META-INF/{JER_AT}" not in jer:
        raise ValueError("Upstream JER is missing its access transformer.")
    if "META-INF/licenses/jer/LICENSE.md" not in ceqol:
        raise ValueError("The CE QoL base JAR must include the upstream JER license.")

    modules = {name: data for name, data in jer.items() if name not in JER_IGNORED}
    collisions = ceqol.keys() & modules.keys()
    if collisions:
        raise ValueError("CE QoL and JER contain overlapping entries: " + ", ".join(sorted(collisions)))
    ceqol.update(modules)
    ceqol[MANIFEST] = manifest + f"\r\nFMLAT: {JER_AT}\r\n\r\n".encode()
    ceqol[METADATA] = (json.dumps(metadata + json.loads(jer[METADATA]), ensure_ascii=False, indent=2) + "\n").encode()

    output.parent.mkdir(parents=True, exist_ok=True)
    with NamedTemporaryFile(dir=output.parent, suffix=".jar.tmp", delete=False) as temp:
        staged = Path(temp.name)
    try:
        with ZipFile(staged, "w") as jar:
            for name, data in sorted(ceqol.items(), key=lambda entry: (entry[0] != MANIFEST, entry[0])):
                jar.writestr(ZipInfo(name), data, compress_type=ZIP_DEFLATED)
        staged.replace(output)
    finally:
        staged.unlink(missing_ok=True)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("ceqol", type=Path, help="reobfuscated CE QoL JAR")
    parser.add_argument("jer", type=Path, help="official JER 1.12.2-0.9.2.60 release JAR")
    parser.add_argument("output", type=Path, help="combined JAR")
    args = parser.parse_args()
    bundle(args.ceqol, args.jer, args.output)
