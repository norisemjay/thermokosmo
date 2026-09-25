#!/data/data/com.termux/files/usr/bin/bash

REPORT="tala_bini_code_report_$(date +%Y%m%d_%H%M%S).txt"

exec > >(tee "$REPORT") 2>&1

echo "============================================================"
echo "              TALA BINI CODE REPORT"
echo "============================================================"
echo "Generated: $(date)"
echo

section() {
    echo
    echo "============================================================"
    echo " $1"
    echo "============================================================"
}

section "PROJECT ROOT"

pwd
ls -lah

section "GRADLE CONFIGURATION"

for f in \
    "build.gradle" \
    "settings.gradle" \
    "gradle.properties" \
    "app/build.gradle"
do
    if [ -f "$f" ]; then
        echo
        echo "---------------- $f ----------------"
        cat "$f"
    fi
done

section "ANDROID MANIFEST"

if [ -f "app/src/main/AndroidManifest.xml" ]; then
    cat "app/src/main/AndroidManifest.xml"
fi

section "JAVA SOURCE FILES"

find app/src/main -type f \
    \( -name "*.java" -o -name "*.kt" -o -name "*.kts" \) \
    ! -path "*/build/*" \
    -print | sort | while read -r file
do
    echo
    echo "============================================================"
    echo " FILE: $file"
    echo "============================================================"
    cat "$file"
done

section "RESOURCES"

find app/src/main/res -type f 2>/dev/null \
    ! -path "*/build/*" \
    -print | sort | while read -r file
do
    echo
    echo "------------------------------------------------------------"
    echo " RESOURCE: $file"
    echo "------------------------------------------------------------"

    case "$file" in
        *.xml|*.json|*.txt|*.html|*.css|*.js)
            cat "$file"
            ;;
        *)
            echo "[Binary/non-text file - content not displayed]"
            ;;
    esac
done

section "ASSETS"

find app/src/main/assets -type f 2>/dev/null \
    -print | sort | while read -r file
do
    echo
    echo "============================================================"
    echo " ASSET: $file"
    echo "============================================================"

    case "$file" in
        *.txt|*.json|*.xml|*.csv|*.html|*.css|*.js)
            cat "$file"
            ;;
        *)
            echo "[Binary/non-text file - content not displayed]"
            ;;
    esac
done

section "ALL PROJECT FILES"

find app/src/main -type f \
    ! -path "*/build/*" \
    -print | sort

section "GRADLE WRAPPER"

if [ -f "./gradlew" ]; then
    echo "gradlew exists."
    ls -lh ./gradlew
    echo
    ./gradlew --version 2>&1
else
    echo "No gradlew file found."
fi

section "BUILD OUTPUT"

if [ -d "app/build" ]; then
    find app/build -maxdepth 5 -type f \
        \( -name "*.apk" -o -name "*.aab" \) \
        -exec ls -lh {} \;
else
    echo "No app/build directory."
fi

section "END"

echo
echo "Report:"
echo "$REPORT"

echo
echo "Review the report before sending it."
echo "============================================================"
