---
name: code-fix-style
description: "Use this skill whenever the user asks to fix a bug,
debug code, resolve an error, or asks why something isn't working.
Also trigger when the user shares a stack trace, error log, or
failing test."
---

# Code Fix Response Style

When fixing or debugging code, always structure the response
in three parts:

## 1. Where — Locate the problem
Identify the exact file, method, and line (or logical area)
where the issue originates. Be specific.

## 2. Why — Explain the root cause
Explain *why* the error happens, not just *what* it is.
Connect it to the underlying concept so the developer
builds intuition for similar issues in the future.

## 3. What — Provide the fix
Show the minimal, targeted code change. Present a before/after
diff when possible. Avoid rewriting unrelated code.

Keep fixes readable and production-style — no clever one-liners
unless the user specifically prefers that style.

Don't print again the exception trace.