import subprocess, re, sys, os

global_change = re.compile(r"^(?:(?:build|settings)\.gradle(?:\.kts)?|gradle\.properties|\.github\/(?:workflows\/.+|getchanged\.py)|\.gradle\/.+)$")
plugin_change = re.compile(r"^(.+)\/(?:src\/main\/.+|(?:build|settings)\.gradle(?:\.kts)?)$")

sha = sys.argv[1]
out = subprocess.run(["git", "diff", "--name-only", sha, f"{sha}~1"], stdout=subprocess.PIPE, cwd=f"{os.environ['GITHUB_WORKSPACE']}/src").stdout.strip(b'\n').decode("utf-8")

matched = ""
found = []
for file in out.split("\n"):
    global_match = global_change.match(file)
    if global_match:
        print("make generateUpdaterJson --no-daemon")
        exit(0)
    plugin_match = plugin_change.match(file)
    if plugin_match:
        name = plugin_match.group(1)
        if name not in found:
            matched = matched + f":{name}:make "
            found.append(name)

print(f"{matched}generateUpdaterJson --no-daemon")