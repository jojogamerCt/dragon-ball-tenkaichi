# Dragon Ball: Tenkaichi

> 🤖 **Questo progetto è stato creato da Claude** (Claude Opus 5.5, modello AI di Anthropic) tramite
> [Claude Code](https://claude.com/claude-code): codice, architettura multi-loader, asset generati
> via script (texture, suoni, lingue), test e documentazione sono stati scritti da Claude su richiesta
> del proprietario della repository.

Mod RPG ispirata a Dragon Ball per **Minecraft Java 26.3**, per **Fabric** e **NeoForge** da un'unica
base di codice. Progetto amatoriale non ufficiale, non affiliato con Bird Studio, Shueisha, Toei
Animation o Bandai Namco. Tutti gli asset (texture, modelli, suoni) sono originali e generati dagli
script in `tools/`.

## Stato

Versione **0.1.0 — vertical slice**: creazione del personaggio, Saiyan e Umani, 12 statistiche,
Battle Power, combattimento corpo a corpo con combo/parate/schivate, lock-on, volo, carica del Ki,
aura 3D, Ki Blast, Kamehameha, Beam Clash, Super Saiyan, Maestro allenatore, Predone Saiyan con IA,
HUD, sincronizzazione multiplayer, salvataggio e configurazione. Dettagli in `docs/FEATURES.md`,
stato delle verifiche in `docs/PROGRESS.md` e `docs/TESTING.md`, limiti in `docs/KNOWN_ISSUES.md`.

## Requisiti

- JDK 25 (lo script usa automaticamente il runtime Java 25 del launcher Minecraft se presente)
- Fabric: Fabric Loader ≥ 0.19.5 e Fabric API 0.162.0+26.3
- NeoForge: 26.3.0.71-beta o successiva

## Build

```bash
./scripts/gw.sh build
```

Su Windows da PowerShell: `scripts\gw.ps1 build`. Lo script imposta Java 25 e il trust store di
Windows (necessario se un antivirus intercetta HTTPS). I due JAR finali vengono copiati in
`build/release/`:

- `dragon-ball-tenkaichi-0.1.0-26.3-fabric.jar`
- `dragon-ball-tenkaichi-0.1.0-26.3-neoforge.jar`

## Test

```bash
./scripts/gw.sh :common:test
./scripts/gw.sh :fabric:runServer -PdbtSmoke
./scripts/gw.sh :neoforge:runServer -PdbtSmoke
./scripts/gw.sh :fabric:runClient -PdbtSmoke
```

Gli smoke test si attivano solo con `-PdbtSmoke` (proprietà di sistema `dbtenkaichi.smokeTest` /
`dbtenkaichi.clientSmoke`) e terminano da soli. Il test client richiede il mondo
`runs/client/saves/dbt_test` (vedi `docs/TESTING.md`) e salva screenshot in `runs/client/screenshots`.

## Comandi di gioco (default, tutti riassegnabili)

| Tasto | Azione |
|---|---|
| R (tieni) | Carica Ki — durante un Beam Clash: battito |
| G | Volo on/off (Spazio sale, Shift scende, Scatto = supersonico) |
| H | Posizione di combattimento (sinistro: combo / tieni = colpo pesante, destro: parata) |
| Alt sinistro | Schivata direzionale / vanish alle spalle del bersaglio agganciato |
| Z | Scatto (verso il bersaglio agganciato) |
| J / K | Aggancia / bersaglio successivo |
| V / B | Trasformazione / annulla |
| X, C, N, M | Slot tecniche 1–4 (tieni per caricare i raggi) |
| U | Menu personaggio |

## Licenza

Tutti i diritti riservati (vedi `LICENSE`). Il template di build deriva da MultiLoader-Template (CC0).
