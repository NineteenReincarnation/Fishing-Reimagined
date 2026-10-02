# Fishing line physics

The line renderer now uses a persistent client-side rope simulation instead of a one-shot analytical sag curve.

## Model

- 16 rope segments
- Verlet-style point integration
- gravity on interior points
- damped velocity from previous-frame positions
- iterative distance constraints
- dynamic total rope length based on synchronized tension
- per-point collision against block collision shapes
- nearest-face collision resolution
- high-tension vibration

The hook and the player's hand remain fixed endpoints.

Low tension gives the rope additional length, so it naturally drapes and can rest against terrain. As tension increases the allowed rope length approaches the direct endpoint distance, pulling the segments straight.

The simulation is presentation-only. It does not decide catches, escapes or line breaks; those remain server-authoritative.

This is an independent implementation inspired by the publicly described goals of Enchanted Fishing Line: gravity sag, dynamic slack, and block collision. No source code or assets from that All Rights Reserved mod are used.
