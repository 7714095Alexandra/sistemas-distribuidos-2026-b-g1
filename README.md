# Distributed Systems - 2026-B - Group 1

Class repository - Corporacion Universitaria del Huila (CORHUILA).

| | |
| --- | --- |
| **Course** | Distributed Systems (cod. 82739) |
| **Group** | 1 |
| **Room** | C5-508 [Sala de Sistemas] - Prado Alto |
| **Schedule** | Martes 6:30-8:10 p.m. y Miercoles 6:30-8:10 p.m. |
| **Term** | 2026-B |

## Structure

Each `NN-week/` contains:

```
NN-week/
|-- 01-session/   # instructor in-class material (Session 1 - weekly)
|-- 02-session/   # instructor in-class material (Session 2 - planning)
\-- hu-status/    # YOUR weekly individual delivery (graded automatically)
```

Do not delete the `01-session` / `02-session` folders: the instructor pushes in-class
material there, and keeping them stable avoids conflicts when you update your fork.

## Weekly individual delivery
Fill `NN-week/hu-status/README.md` in your fork (keep the exact structure - it is parsed
automatically). Create your profile repo `username/username` with a CONFIG block:

```
<!-- CONFIG
FULL_NAME: Your Full Name
GITHUB_USER: your-github-user
-->
```

## Git workflow (per environment)
develop -> hu-xxx-dev -> PR to develop; qa -> hu-xxx-qa -> PR to qa; main -> hu-xxx-main -> PR to main.
Conventional Commits: `type(scope): summary`.

Learning material (OVAs): https://code-corhuila.github.io/ova-web/2026-B/distribuidos/
