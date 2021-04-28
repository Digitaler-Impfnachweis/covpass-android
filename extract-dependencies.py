import re
from pathlib import Path
from subprocess import check_output
import argparse
import platform

ROOT = Path(__file__).parent
GRADLEW = str((ROOT / 'gradlew').resolve())

APP_MODULES = 'APP'
TEST_MODULES = 'TEST'
ALL_CONFIG = 'ALL'

all_modules = [x.name for x in ROOT.iterdir()
               if x.is_dir() and (x / 'build.gradle').is_file() and (x / 'src').is_dir()]

deps_re = re.compile(r'(?<=--- )(?:[a-z][a-z0-9\.\-_]+:)+[0-9\.]+.*(?=\n)', re.UNICODE)

def read_dependencies(modules, configuration):
    cmd = [GRADLEW]
    for module in modules:
        cmd.append(module + ':dependencies')
        if configuration != ALL_CONFIG:
            cmd.extend(['--configuration', configuration])
    output = check_output(
        cmd,
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
    by_config = {}
    for module in modules:
        module_configurations = configurations or get_default_configurations(module)
        for configuration in module_configurations:
            by_config.setdefault(configuration, []).append(module)
    for configuration, modules in by_config.items():
        all += read_dependencies(modules, configuration)
    return all


def parse_module_names(names, default=()):
    modules = names.split(',') if names else list(default)
    if APP_MODULES in modules:
        modules.remove(APP_MODULES)
        modules.extend(module for module in all_modules if module == 'app' or module.startswith('app-'))
    if TEST_MODULES in modules:
        modules.remove(TEST_MODULES)
        modules.extend(module for module in all_modules if module == 'test' or module.endswith('-test'))
    return modules


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--modules", type=str, default=None)
    parser.add_argument("--exclude-modules", type=str, default=None)
    parser.add_argument("--configurations", type=str, default=None)
    args = parser.parse_args()

    modules = set(parse_module_names(args.modules, all_modules)) - set(parse_module_names(args.exclude_modules))
    raw_entries = read_all(modules, args.configurations and args.configurations.split(','))
    normalized_entries = normalize_versions(raw_entries)
    deduplicated = sorted(set(normalized_entries))
    print('\n'.join(deduplicated))


if __name__ == '__main__':
    main()
