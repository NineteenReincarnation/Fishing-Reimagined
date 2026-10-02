# Versioning

Fishing Reimagined follows the same project-version convention as Argon:

`X.Y.Z+<minecraft-version>`

The project starts on the **1.1** line. There is no 1.0 line.

Current development target: `1.1.16+26.2`.

- `X` — major project/content generation.
- `Y` — release line.
- `Z` — iterations and fixes within that line.
- The suffix records the Minecraft version targeted by that artifact.

Minecraft compatibility changes do not require an architecture branch. Version-specific boundaries stay explicit in code only where upstream API or bytecode differences require them.
