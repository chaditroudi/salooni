const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

function run(cmd, label) {
  console.log(`\n===== ${label} =====`);
  console.log(`> ${cmd}`);
  try {
    const out = execSync(cmd, { encoding: 'utf8', cwd: __dirname });
    console.log(out || '(no output)');
    return out;
  } catch (e) {
    console.log('STDERR:', e.stderr || '');
    console.log('STDOUT:', e.stdout || '');
    console.log('EXIT CODE:', e.status);
    return e.stdout || '';
  }
}

// Step 1
run('git --no-pager status glowzi-identity-service/ --short', 'Step 1a: git status');
run('git --no-pager diff --cached --name-only', 'Step 1b: staged files');

// Step 2: Check and delete temp files
const tempFiles = ['write-docs.js', 'extract_docs.py', 'cleanup.py'];
for (const f of tempFiles) {
  const fp = path.join('glowzi-identity-service', f);
  // Check if tracked
  let tracked = false;
  try {
    execSync(`git ls-files --error-unmatch "${fp}"`, { encoding: 'utf8', cwd: __dirname });
    tracked = true;
  } catch {
    tracked = false;
  }
  if (tracked) {
    run(`git rm "${fp}"`, `Step 2: git rm ${f} (tracked)`);
  } else {
    // Just delete from filesystem
    const fullPath = path.join(__dirname, fp);
    if (fs.existsSync(fullPath)) {
      fs.unlinkSync(fullPath);
      console.log(`\n===== Step 2: Deleted ${f} (untracked) =====`);
    } else {
      console.log(`\n===== Step 2: ${f} does not exist =====`);
    }
  }
}

// Step 3
run('git add glowzi-identity-service/IDENTITY-SERVICE-DOCS.md', 'Step 3: Stage docs file');

// Step 4
run('git --no-pager diff --cached --name-only', 'Step 4: Verify staged files');

// Step 5: Write commit message to file and commit
const commitMsg = `docs(identity): rewrite IDENTITY-SERVICE-DOCS.md as pre-production technical document

Replace the 2651-line outdated documentation with a comprehensive 484-line
pre-production technical document covering:

- Service overview and hexagonal architecture layers
- Complete API reference (7 endpoints) with validation rules
- Security model (OAuth2/Keycloak authentication flow)
- Database schema (Flyway migrations V1-V4)
- Keycloak integration details
- Configuration reference (application.yml + Docker env vars)
- Test suite overview (unit, integration, architecture tests)
- Deployment guide (local + Docker)
- Full file structure map
- Pre-production changelog

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>
`;

const msgFile = path.join(__dirname, 'commit-msg.txt');
fs.writeFileSync(msgFile, commitMsg, 'utf8');
run('git commit -F commit-msg.txt', 'Step 5: Commit');

// Cleanup
fs.unlinkSync(msgFile);
console.log('\n===== Cleanup: removed commit-msg.txt =====');

// Self-cleanup
fs.unlinkSync(path.join(__dirname, 'run_git_ops.js'));
console.log('===== Cleanup: removed run_git_ops.js =====');
