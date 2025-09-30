# PR Creation Rules
This document describes the format and rules for creating a Pull Request (PR) on GitHub.

# Execution Details
## Commit Guidelines
- Use imperative mood for commit messages (e.g., "Fix bug" instead of "Fixed
- Write Commit messages in English

## PR Guidelines
- If there are unstaged changes when creating a pull request, please split them appropriately and commit them separately.
- Use the [PR Template](../templates/PR_TEMPLATE.md) for PR descriptions
- PR titles should be concise and descriptive
- Ensure all tests pass before submitting a PR
- Write PR title and descriptions in English

## Command to Execute

To create a PR, execute the following command. Add options as needed to construct the title, body, etc.
```bash
gh pr create
```

The rules for describing the content of the PR are as follows.
## Title
Follow these rules for the PR title:
- Write the title in English.
- Keep the title concise and clearly indicate the changes.

## Body
Follow these rules for the PR body:
- Write the body in English.
- It is recommended to divide the body into the following sections:
  - Refer to the [PR Template](../templates/PR_TEMPLATE.md) for the PR body template.
- Briefly explain the purpose and impact of the changes.
- Include related issue numbers or ticket numbers, if possible.

## Labels
Labels are automatically assigned by GitHub Actions, so no manual configuration is required.
