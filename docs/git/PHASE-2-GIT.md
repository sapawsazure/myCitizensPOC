# Phase 2 — Git commands

Phase 2 adds **Expenses** and **Habits** (+2 features, **5 in total**). You build it on a feature branch and then **merge it into `main`**.
These commands work unchanged in **Windows PowerShell** and **Git Bash**. Run them yourself; nothing here was executed for you.

Repository: `https://github.com/sapawsazure/myCitizensPOC.git` · Target branch: `main` · Feature branch: `feature/phase-2-expenses-habits` · Folder: `productivity-hub`

Files this phase **changed** (everything else is new): `README.md` (status line, two feature rows), `backend/pom.xml` (version 2.0.0),
`frontend/package.json` and `frontend/package-lock.json` (version 2.0.0). Phase 1 code, migrations and tests are untouched.

## 1. Branch, commit and push the feature branch

Puts the Phase 2 files on their own branch and uploads that branch to GitHub.

```bash
cd productivity-hub
git branch --show-current
# Expected: feature/phase-2-expenses-habits.
# When Phase 2 was generated you were on "newbr" (the same commit as main). If you are still on newbr (or main), create the branch now - your uncommitted Phase 2 files come along:
git switch -c feature/phase-2-expenses-habits
git add .
git status
git commit -m "feat: Phase 2 - Expenses and Habits (+2 features, total 5)"
git push -u origin feature/phase-2-expenses-habits
```

`git status` before the commit should show **4 modified files** (`README.md`, `backend/pom.xml`, `frontend/package.json`,
`frontend/package-lock.json`) plus **new files only**: `V2_1/V2_2/V2_9` migrations, the `expenses` and `habits` folders (backend, tests,
frontend) and `docs/git/PHASE-2-GIT.md`. It must **not** list `.env`, `node_modules/`, `backend/target/` or `backend/src/main/resources/static/`.
Messages such as "LF will be replaced by CRLF" are harmless.

> Prefer to keep using `newbr` as your feature branch? Skip the `git switch -c` line and replace `feature/phase-2-expenses-habits` with `newbr` in every command below.

## 2. Merge into main and tag

### Option A — local merge (default: test first, push last)

Merges the feature branch into `main` with a visible merge commit, lets you test the result, and only then publishes it.

```bash
git fetch origin
git switch main
git pull origin main
# Optional dry run (Git 2.38+): prints a tree id when the merge is clean, otherwise lists the conflicting files
git merge-tree --write-tree --name-only main feature/phase-2-expenses-habits
git merge --no-ff feature/phase-2-expenses-habits -m "Merge Phase 2: Expenses and Habits (total 5 features)"
```

**Now test the merged result BEFORE pushing.** Stop any app that is still running on port 8180 (Ctrl+C in its terminal), then:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\run.ps1
```

In a second terminal run `node scripts/smoke.mjs --expect 5` and open http://localhost:8180 — you should see **5** features
(Tasks, Notes, Bookmarks, Expenses, Habits). On start-up Flyway adds the Phase 2 tables to your existing database automatically.
When you are happy, publish:

```bash
git push origin main
git tag -a v2.0.0 -m "Phase 2: 5 features"
git push origin v2.0.0
git log --oneline --graph --decorate -n 10
```

The graph should show the merge commit on `main`, with the Phase 2 commit as its second parent and the tag `v2.0.0`.

### Option B — Pull Request (GitHub CLI `gh`)

Same result through GitHub. Without `gh`, open the pull request on github.com and press **Create a merge commit** (not "Squash" and not "Rebase").

```bash
gh pr create --base main --head feature/phase-2-expenses-habits --title "Phase 2: Expenses and Habits" --body "Adds Expenses and Habits (+2 features, total 5)."
gh pr merge feature/phase-2-expenses-habits --merge
git switch main
git pull origin main
git tag -a v2.0.0 -m "Phase 2: 5 features"
git push origin v2.0.0
```

Then run the test steps from Option A on the updated `main`.

## 3. When something goes wrong

```bash
git status                          # during a conflict: files marked "both modified"
# fix the <<<<<<< ======= >>>>>>> markers in each file, keep the intent of both sides, then:
git add <file>
git merge --continue                # finish the merge
git merge --abort                   # or cancel the whole merge
git revert -m 1 <merge-commit-sha>  # undo an already-pushed merge without rewriting history, then: git push origin main
docker compose down -v              # reset the dev database before running an older tag (Flyway refuses old code on a newer schema)
```

Going back to `v1.0.0` after you ran Phase 2 needs the last line (`docker compose down -v`), because your database then contains
migrations 2.1 to 2.9 that the old code does not know. It deletes the dev data; the seed data comes back on the next start.

## 4. Optional drill — see a real 3-way merge conflict

Safe: it only uses two throw-away branches and never touches `main`. Start from a clean working tree (everything committed).

```bash
git switch main
git switch -c drill/a
# edit line 1 of README.md (the title) to say A, then:
git commit -am "drill: A"
git switch main
git switch -c drill/b
# edit the SAME line 1 of README.md to say B, then:
git commit -am "drill: B"
git merge drill/a
# CONFLICT in README.md -> open it, keep the line you want, delete the <<<<<<< ======= >>>>>>> lines, then:
git add README.md
git merge --continue
git switch main
git branch -D drill/a drill/b
```

Do **not** push the drill branches.

## Next step

When `main` has the merge and the tag `v2.0.0`, ask Sonnet to "execute Phase 3". Before that you can prepare its branch with
`git switch -c feature/phase-3-contacts-calendar-timetracker`.
