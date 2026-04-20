@echo off
cd /d "%~dp0"

echo ===== Step 1a: git status =====
git --no-pager status glowzi-identity-service/ --short

echo.
echo ===== Step 1b: staged files =====
git --no-pager diff --cached --name-only

echo.
echo ===== Step 2: Delete temp files =====
git ls-files --error-unmatch "glowzi-identity-service/write-docs.js" >nul 2>&1
if %errorlevel% equ 0 (
    git rm "glowzi-identity-service/write-docs.js"
) else (
    del "glowzi-identity-service\write-docs.js" 2>nul && echo Deleted write-docs.js (untracked) || echo write-docs.js not found
)

git ls-files --error-unmatch "glowzi-identity-service/extract_docs.py" >nul 2>&1
if %errorlevel% equ 0 (
    git rm "glowzi-identity-service/extract_docs.py"
) else (
    del "glowzi-identity-service\extract_docs.py" 2>nul && echo Deleted extract_docs.py (untracked) || echo extract_docs.py not found
)

git ls-files --error-unmatch "glowzi-identity-service/cleanup.py" >nul 2>&1
if %errorlevel% equ 0 (
    git rm "glowzi-identity-service/cleanup.py"
) else (
    del "glowzi-identity-service\cleanup.py" 2>nul && echo Deleted cleanup.py (untracked) || echo cleanup.py not found
)

echo.
echo ===== Step 3: Stage docs file =====
git add glowzi-identity-service/IDENTITY-SERVICE-DOCS.md

echo.
echo ===== Step 4: Verify staged files =====
git --no-pager diff --cached --name-only

echo.
echo ===== Step 5: Commit =====
git commit -F commit-msg.txt

echo.
echo ===== Cleanup =====
del commit-msg.txt
del run_git_ops.js 2>nul
del git-ops.bat
echo Done! All temporary scripts cleaned up.
