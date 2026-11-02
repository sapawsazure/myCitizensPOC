# Phase 3 — Git commands

Phase 3 adds **Contacts**, **Calendar** and **Time Tracker** (+3 features, **8 in total**: 3 + 2 + 3). You build it on a feature branch and then **merge it into `main`**.
These commands work unchanged in **Windows PowerShell** and **Git Bash**. Run them yourself; nothing here was executed for you.

Repository: `https://github.com/sapawsazure/myCitizensPOC.git` · Target branch: `main` · Feature branch: `feature/phase-3-contacts-calendar-timetracker` · Folder: `productivity-hub`

Files this phase **changed** (everything else is new): `README.md` (status line, three feature rows, smoke example), `backend/pom.xml` (version 3.0.0),
`frontend/package.json` and `frontend/package-lock.json` (version 3.0.0). Phase 1 and Phase 2 code, migrations and tests are untouched.

## 1. Branch, commit and push the feature branch

Puts the Phase 3 files on their own branch and uploads that branch to GitHub.

```bash
cd productivity-hub
git branch --show-current
# Expected: feature/phase-3-contacts-calendar-timetracker.
# When Phase 3 was generated you were on "main". If you are still on main, create the branch now - your uncommitted Phase 3 files come along:
git switch -c feature/phase-3-contacts-calendar-timetracker
git add .
git status
git commit -m "feat: Phase 3 - Contacts, Calendar, Time Tracker (+3 features, total 8)"
git push -u origin feature/phase-3-contacts-calendar-timetracker
```

`git status` before the commit should show **4 modified files** (`README.md`, `backend/pom.xml`, `frontend/package.json`,
`frontend/package-lock.json`) plus **new files only**: the `V3_1/V3_2/V3_3/V3_9` migrations, the `contacts`, `calendar` and `timetracker`
folders (backend, tests, frontend) and `docs/git/PHASE-3-GIT.md`. It must **not** list `.env`, `node_modules/`, `backend/target/` or
`backend/src/main/resources/static/`. Messages such as "LF will be replaced by CRLF" are harmless.

## 2. Merge into main and tag

### Option A — local merge (default: test first, push last)

Merges the feature branch into `main` with a visible merge commit, lets you test the result, and only then publishes it.

```bash
git fetch origin
git switch main
git pull origin main
# Optional dry run (Git 2.38+): prints a tree id when the merge is clean, otherwise lists the conflicting files
git merge-tree --write-tree --name-only main feature/phase-3-contacts-calendar-timetracker
git merge --no-ff feature/phase-3-contacts-calendar-timetracker -m "Merge Phase 3: Contacts, Calendar and Time Tracker (total 8 features)"
```

**Now test the merged result BEFORE pushing.** Stop any app that is still running on port 8180 (Ctrl+C in its terminal), then:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\run.ps1
```

In a second terminal run `node scripts/smoke.mjs --expect 8` and open http://localhost:8180 — you should see **8** features
(Tasks, Notes, Bookmarks, Expenses, Habits, Contacts, Calendar, Time Tracker). On start-up Flyway adds the Phase 3 tables to your
existing database automatically. When you are happy, publish:

```bash
git push origin main
git tag -a v3.0.0 -m "Phase 3: 8 features"
git push origin v3.0.0
git log --oneline --graph --decorate -n 10
```

### Option B — Pull Request (GitHub CLI `gh`)

Same result through GitHub. Without `gh`, open the pull request on github.com and press **Create a merge commit** (not "Squash" and not "Rebase").

```bash
gh pr create --base main --head feature/phase-3-contacts-calendar-timetracker --title "Phase 3: Contacts, Calendar and Time Tracker" --body "Adds Contacts, Calendar and Time Tracker (+3 features, total 8)."
gh pr merge feature/phase-3-contacts-calendar-timetracker --merge
git switch main
git pull origin main
git tag -a v3.0.0 -m "Phase 3: 8 features"
git push origin v3.0.0
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

## 4. Show the whole journey

After the Phase 3 merge, these commands tell the story of the POC: three releases, two merge commits.

```bash
git log --oneline --graph --decorate --all
git diff --stat v1.0.0 v2.0.0
git diff --stat v2.0.0 v3.0.0
```

The second and third commands show that each phase **only added files** (plus the small version and README edits).

### Time travel: run the 3-feature version again, then come back

The database of the newer version contains tables the old code does not know, and Flyway refuses to start in that case,
so reset the dev database whenever you switch versions. **This deletes the dev data** (seed data comes back automatically).

```bash
git switch --detach v1.0.0
docker compose down -v
# ... run scripts\run.ps1, open http://localhost:8180 -> 3 features ...
git switch main
docker compose down -v
# ... run scripts\run.ps1 again -> 8 features
```

Do the same with `v2.0.0` to see 5 features. Stop the running app (Ctrl+C) before each `git switch`.

## 5. Expected history on GitHub after Phase 3

```text
*   Merge Phase 3: Contacts, Calendar and Time Tracker (total 8 features)   <- v3.0.0
|\
| * feat: Phase 3 - Contacts, Calendar, Time Tracker (+3 features, total 8)
|/
*   Merge Phase 2: Expenses and Habits (total 5 features)                   <- v2.0.0
|\
| * feat: Phase 2 - Expenses and Habits (+2 features, total 5)
|/
* feat: Phase 1 baseline - Tasks, Notes, Bookmarks (3 features)             <- v1.0.0
```

The feature branches stay on GitHub (`feature/phase-2-expenses-habits`, `feature/phase-3-contacts-calendar-timetracker`), so you can open
them to see exactly what each phase contributed. That completes the POC.
