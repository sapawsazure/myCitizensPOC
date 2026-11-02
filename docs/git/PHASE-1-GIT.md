# Phase 1 — Git commands

Phase 1 creates the repository content (Tasks, Notes, Bookmarks = **3 features**) and pushes it to `main`.
These commands work unchanged in **Windows PowerShell** and **Git Bash**. Run them yourself; nothing here was executed for you.

Repository: `https://github.com/sapawsazure/myCitizensPOC.git` · Target branch: `main` · Folder: `productivity-hub`

## 0. One-time setup (skip what is already done)

Tells Git who you are, and prepares the GitHub side.

```bash
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
# Create the EMPTY repository on github.com (no README, .gitignore or license), or: gh repo create sapawsazure/myCitizensPOC --private
# Authenticate once with: gh auth login   (or use an SSH remote). Never paste tokens into the remote URL.
```

## 1. Commit Phase 1 and push it to main

Turns the `productivity-hub` folder into a Git repository, commits everything and pushes it to GitHub.
Run it from the folder that contains `productivity-hub` (here: `e:\Git_Merge_Changes`).

```bash
cd productivity-hub
git init -b main
git remote add origin https://github.com/sapawsazure/myCitizensPOC.git
git add .
git update-index --chmod=+x backend/mvnw scripts/run.sh
git status
git commit -m "feat: Phase 1 baseline - Tasks, Notes, Bookmarks (3 features)"
git push -u origin main
```

Check `git status` before you commit: it must **not** list `.env`, `node_modules/`, `backend/target/` or
`backend/src/main/resources/static/` (they are git-ignored). Messages such as "LF will be replaced by CRLF" are harmless.

If the GitHub repository is not empty (it already has a README, for example), run these two lines **before** `git push`:

```bash
git fetch origin
git merge origin/main --allow-unrelated-histories
```

## 2. Tag the release

Marks this state as `v1.0.0` so you can come back to the 3-feature version at any time.

```bash
git tag -a v1.0.0 -m "Phase 1: 3 features"
git push origin v1.0.0
```

## 3. Check

```bash
git log --oneline --decorate -n 5
```

You should see one commit, decorated with `HEAD -> main`, `origin/main` and `tag: v1.0.0`.

## 4. When something goes wrong

Useful from Phase 2 on, when you start merging.

```bash
git status                          # during a conflict: files marked "both modified"
# fix the <<<<<<< ======= >>>>>>> markers in each file, keep the intent of both sides, then:
git add <file>
git merge --continue                # finish the merge
git merge --abort                   # or cancel the whole merge
git revert -m 1 <merge-commit-sha>  # undo an already-pushed merge without rewriting history, then: git push origin main
docker compose down -v              # reset the dev database before running an older tag (Flyway refuses old code on a newer schema)
```

## Next step

Open http://localhost:8180 (start it with `scripts/run.ps1`) and confirm you see **3** features. Then ask Sonnet to
"execute Phase 2". Before that, create the Phase 2 branch if you want it ready:
`git switch -c feature/phase-2-expenses-habits`.
