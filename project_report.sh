#!/data/data/com.termux/files/usr/bin/bash

set +e

REPORT="termux_project_report_$(date +%Y%m%d_%H%M%S).txt"

exec > >(tee "$REPORT") 2>&1

echo "============================================================"
echo "              TERMUX PROJECT REPORT"
echo "============================================================"
echo "Generated: $(date)"
echo

section() {
    echo
    echo "============================================================"
    echo " $1"
    echo "============================================================"
}

section "SYSTEM"

echo "OS:"
uname -a

echo
echo "Architecture:"
uname -m

echo
echo "Android:"
getprop ro.build.version.release 2>/dev/null

echo
echo "Android SDK:"
getprop ro.build.version.sdk 2>/dev/null

echo
echo "Termux:"
echo "PREFIX=$PREFIX"
echo "HOME=$HOME"

echo
echo "Shell:"
echo "$SHELL"

section "TERMUX PACKAGES"

echo "Installed Termux packages:"
pkg list-installed 2>/dev/null

section "COMMON TOOLS / VERSIONS"

for cmd in \
    git \
    python \
    python3 \
    pip \
    pip3 \
    node \
    npm \
    npx \
    bun \
    deno \
    php \
    ruby \
    perl \
    java \
    javac \
    go \
    rustc \
    cargo \
    gcc \
    g++ \
    make \
    cmake \
    clang \
    curl \
    wget \
    openssl \
    sqlite3 \
    ffmpeg \
    docker
do
    if command -v "$cmd" >/dev/null 2>&1; then
        echo
        echo ">>> $cmd"
        "$cmd" --version 2>&1 | head -n 3
    fi
done

section "PYTHON ENVIRONMENT"

if command -v python >/dev/null 2>&1; then
    python --version 2>&1
    echo
    echo "Python executable:"
    command -v python

    echo
    echo "Installed Python packages:"
    python -m pip list 2>/dev/null
fi

section "NODE ENVIRONMENT"

if command -v node >/dev/null 2>&1; then
    echo "Node:"
    node --version

    echo
    echo "NPM:"
    npm --version 2>/dev/null

    echo
    echo "Global packages:"
    npm list -g --depth=0 2>/dev/null
fi

section "GIT"

if command -v git >/dev/null 2>&1; then
    echo "Git version:"
    git --version

    echo
    echo "Git configuration (sensitive values hidden):"
    git config --list 2>/dev/null | \
        sed -E 's/(token|password|passwd|secret|credential|key)=.*/\1=[REDACTED]/Ig'
fi

section "CURRENT DIRECTORY"

echo "PWD:"
pwd

echo
echo "Directory:"
ls -lah

section "PROJECT STRUCTURE"

echo "Project tree (limited depth):"

if command -v tree >/dev/null 2>&1; then
    tree -a -L 4 \
        -I '.git|node_modules|__pycache__|.venv|venv|.idea|.gradle|build|dist'
else
    find . \
        -maxdepth 4 \
        -type f \
        ! -path './.git/*' \
        ! -path './node_modules/*' \
        ! -path './__pycache__/*' \
        ! -path './.venv/*' \
        ! -path './venv/*' \
        ! -path './build/*' \
        ! -path './dist/*' \
        | sort
fi

section "IMPORTANT PROJECT FILES"

for file in \
    README.md \
    README.txt \
    package.json \
    package-lock.json \
    yarn.lock \
    pnpm-lock.yaml \
    requirements.txt \
    pyproject.toml \
    setup.py \
    Pipfile \
    Cargo.toml \
    go.mod \
    composer.json \
    Gemfile \
    Makefile \
    Dockerfile \
    docker-compose.yml \
    docker-compose.yaml \
    .env.example \
    tsconfig.json \
    vite.config.js \
    vite.config.ts
do
    if [ -f "$file" ]; then
        echo
        echo "---------------- $file ----------------"

        # فایل‌های پیکربندی را بدون نمایش مقدار secrets چاپ می‌کنیم
        sed -E \
            -e 's/(password|passwd|token|secret|api[_-]?key|access[_-]?key|private[_-]?key)[[:space:]]*[:=][[:space:]]*.*/\1=[REDACTED]/Ig' \
            "$file" | head -n 300
    fi
done

section "ENVIRONMENT VARIABLES"

echo "Variable names only; values are NOT shown."

env | cut -d= -f1 | sort

section "PORTS / PROCESSES"

echo "Processes related to common development tools:"

ps -ef 2>/dev/null | \
    grep -E 'python|node|npm|php|java|go|ruby|server|uvicorn|gunicorn' | \
    grep -v grep | head -n 100

section "NETWORK"

echo "IP information:"

ip addr 2>/dev/null | \
    grep -E 'inet |inet6 ' | \
    sed 's/^[[:space:]]*//'

section "STORAGE"

df -h

section "PROJECT FILE COUNTS"

echo "Total files:"
find . \
    -type f \
    ! -path './.git/*' \
    ! -path './node_modules/*' \
    ! -path './__pycache__/*' \
    ! -path './.venv/*' \
    ! -path './venv/*' \
    2>/dev/null | wc -l

echo
echo "Total directories:"
find . \
    -type d \
    ! -path './.git/*' \
    ! -path './node_modules/*' \
    ! -path './__pycache__/*' \
    ! -path './.venv/*' \
    ! -path './venv/*' \
    2>/dev/null | wc -l

section "GIT PROJECT STATUS"

if [ -d ".git" ]; then
    echo "Git repository detected."

    echo
    git status --short 2>/dev/null

    echo
    echo "Current branch:"
    git branch --show-current 2>/dev/null

    echo
    echo "Remote names only:"
    git remote 2>/dev/null
else
    echo "No .git directory detected."
fi

section "POSSIBLE CONFIG / SECRET FILES"

echo "File names only. Contents are NOT displayed."

find . -maxdepth 4 -type f \
    \( \
        -name ".env" \
        -o -name ".env.*" \
        -o -name "*secret*" \
        -o -name "*credential*" \
        -o -name "*password*" \
        -o -name "*token*" \
        -o -name "*.pem" \
        -o -name "*.key" \
        -o -name "*.p12" \
        -o -name "*.jks" \
    \) \
    ! -path './.git/*' \
    ! -path './node_modules/*' \
    2>/dev/null

section "END"

echo
echo "Report saved as:"
echo "$REPORT"

echo
echo "IMPORTANT:"
echo "Review the report before sending it."
echo "Remove any private information if something sensitive appears."
echo
echo "============================================================"
