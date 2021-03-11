import re
from pathlib import Path
from subprocess import check_output
import platform
import sys

ROOT = Path(__file__).parent
GRADLEW = str((ROOT / 'gradlew').resolve())

all_modules = [x.name for x in ROOT.iterdir()
               if x.is_dir() and (x / 'build.gradle').is_file() and (x / 'src').is_dir()]

deps_re = re.compile(r'(?:[a-z][a-z0-9\.\-_]+:)+[0-9\.]+.*(?=\n)', re.UNICODE)

def read_dependencies(module, configuration):
    output = check_output(
        [GRADLEW, module + ':dependencies', '--configuration', configuration],
        shell=platform.system() == 'Windows'
    ).decode()
    content = '\n'.join(output.splitlines()[:-1])
    return deps_re.findall(content)


def normalize_versions(entries):
    normalized = set()
    for entry in entries:
        parts = entry.split(':')
        version = re.findall(r'\d[0-9|\._\-a-zA-Z]*', parts[2])[-1]
        normalized.add(':'.join([parts[0], parts[1], version]))
    return normalized


def get_default_configurations(module):
    if not (ROOT / module / 'src' / 'main' / 'AndroidManifest.xml').exists():
        return ['runtimeClasspath']
    return ['releaseRuntimeClasspath']


def read_all(modules, configurations):
    all = []
    for module in modules:
        module_configurations = configurations or get_default_configurations(module)
        for configuration in module_configurations:
            all = all + read_dependencies(module, configuration)
    return all


def main(modules, configurations):
    raw_entries = read_all(modules, configurations)
    normalized_entries = normalize_versions(raw_entries)
    deduplicated = sorted(set(normalized_entries))
    print('\n'.join(deduplicated))


if __name__ == '__main__':
    main(
        sys.argv[1].split(',') if len(sys.argv) > 1 else all_modules,
        sys.argv[2].split(',') if len(sys.argv) > 2 else None,
    )
