# Architettura — Dragon Ball: Tenkaichi

## Toolchain verificata (11 ottobre 2026)

| Componente | Versione | Fonte |
|---|---|---|
| Minecraft | 26.3 (resource pack 97, data pack 121) | `version.json` del jar 26.3, misode/mcmeta |
| Java | 25 (`java-runtime-epsilon` 25.0.1 del launcher) | `version.json` |
| Gradle | 9.7.1 (checksum wrapper verificato) | services.gradle.org |
| Fabric Loader | 0.19.5 | meta.fabricmc.net |
| Fabric API | 0.162.0+26.3 | Modrinth |
| Fabric Loom | 1.18.4 (`net.fabricmc.fabric-loom`, mojmap senza remap) | maven.fabricmc.net |
| NeoForge | 26.3.0.71-beta (unica linea disponibile per 26.3) | maven.neoforged.net |
| ModDevGradle | 2.0.148 | maven.neoforged.net |
| NeoForm (common) | 26.3-1 | maven.neoforged.net |

Base del build: template **MultiLoader-Template** (jaredlll08, branch 26.3, CC0). Architectury non è
usato: per 26.3 il suo ASM non legge le classi Java 27 di LWJGL 3.4.3, mentre Loom + MDG funzionano
senza workaround.

## Moduli

```
common/    tutta la logica di gioco, rete astratta, dati, client condiviso, mixin, asset e dati
fabric/    entrypoint, servizi piattaforma (registrazioni, rete, allegati), eventi Fabric API
neoforge/  entrypoint, servizi piattaforma (DeferredRegister, PayloadRegistrar, AttachmentType), eventi
build-logic/  convenzioni Gradle condivise (multiloader-common / multiloader-loader)
tools/     generatori riproducibili di asset (lingue, texture, suoni)
scripts/   wrapper Gradle per Windows (JDK 25 + trust store di Windows)
docs/      documentazione di progetto
```

`common` viene compilato contro Minecraft vanilla (NeoForm) e i suoi sorgenti vengono ricompilati
dentro ogni loader. Nessuna classe di `common` importa API di Fabric o NeoForge.

## Seam piattaforma (`com.dbtenkaichi.platform`)

Caricato via `ServiceLoader` (`META-INF/services`) in ogni loader:

- `IPlatform` — nome loader, cartella config, mod caricate, dati giocatore (allegato persistente),
  permesso di rompere blocchi (inoltrato a `PlayerBlockBreakEvents.BEFORE` / `BreakBlockEvent`, così
  le mod di protezione/claim possono bloccare la distruzione ambientale).
- `INetwork` — invio a giocatore, a chi traccia un'entità, al server.
- `IRegistration` — registrazione immediata (Fabric) o differita (`DeferredRegister`, NeoForge),
  builder della tab creativa.

## Dati del giocatore

`PlayerData` (persistente, schema versionato con `PlayerDataMigrations`) è salvato come allegato:
Fabric `AttachmentRegistry` (persistent + copyOnDeath), NeoForge `AttachmentType` (serialize +
copyOnDeath). Contiene razza, aspetto, 12 statistiche, TP, Ki, stamina, fatica, forma attuale,
padronanza, tecniche, slot, progressi di pratica, contatori giornalieri anti-grinding, Zenkai.

`RuntimeState` (non salvato) contiene lo stato di combattimento autoritativo: volo, carica,
parata, combo, stun, invulnerabilità, lock-on, raggio attivo, beam clash, sessioni di allenamento,
flag di sincronizzazione e limiti di frequenza.

## Rete

Pacchetti in `network/Packets` con `StreamCodec` espliciti. Client→server: `CreateCharacter`,
`Action` (enum compatto + argomento), `AssignSlot`, `TrainingInput`. Ogni pacchetto è validato
sul server e limitato a `maxActionsPerSecond`. Server→client: `SyncData` (snapshot completo solo
al proprietario), `Vitals` (Ki/stamina/BP a soglia), `Appearance` e `EntityState` (a chi traccia),
`Anim`, `CombatFx`, `Forms`, `ServerConfig`, `Clash`, `Cinematic`, `OpenScreen`.

## Autorità del server

Danni, Ki, stamina, trasformazioni, ricompense, tecniche, beam clash e distruzione sono calcolati
solo sul server. Il client predice il movimento (volo, dash, schivate) usando la fisica vanilla con
collisioni; il server verifica velocità e costi Ki, ferma il volo in caso di violazioni ripetute e
neutralizza il kick "volo non consentito" tramite un mixin mirato.

## Sistemi principali (common)

| Pacchetto | Contenuto |
|---|---|
| `stats`, `progression`, `combat/DamageMath` | Formule pure e configurabili (testate con JUnit) |
| `combat` | Combo, colpi pesanti contestuali, parata/perfect block, guard break, clash, schivata, vanish, dash, knockdown |
| `ki` | Carica, tecniche (`Techniques`), raggi/sfere, `BeamClashManager` |
| `form` | Trasformazioni data-driven da datapack (`data/<ns>/tenkaichi_forms/*.json`), sblocco per rabbia, padronanza |
| `movement` | Volo autoritativo, costi, atterraggi con onda d'urto |
| `training` | Guadagni passivi con rendimenti decrescenti, pratica per statistica, sessioni con il Maestro |
| `entity` | `KiBeamEntity`, `KiBlastEntity`, `FighterNpc`, `MasterEntity`, `RaiderEntity` + `ai/RaiderBrain` (FSM gerarchica) |
| `world` | `DestructionManager` con modalità off/visual/real/regenerating, budget per tick, protezioni |
| `client` | Stato sincronizzato, input contestuale, volo predetto, lock-on, animazioni, render, HUD, schermate |

## Rendering (client)

- Animazioni: `client/anim` — clip a keyframe con easing + stati continui fusi con pesi; applicate
  con mixin a `PlayerModel.setupAnim` (vale anche per i modelli di capelli/coda/aura e per gli NPC
  che usano `AvatarRenderState`). Rotazioni del corpo intero (volo, roll) in `setupRotations`.
- Capelli: 10 stili geometrici procedurali (`TenkaichiModels`), colorati per vertice, emissivi in forma Super Saiyan.
- Skin: texture 64×64 generata a runtime (`CustomSkins`) da tono pelle, occhi, sopracciglia,
  espressione, abito; cache LRU, opzione per usare la propria skin.
- Aura: guscio energy-swirl gonfiato + lingue di fiamma 3D additive (pipeline `lightning`),
  scalate per qualità grafica; particelle con budget.
- Raggi: cilindri multistrato additivi con spirali, testa e bocca di fuoco; culling esteso.

## Configurazione

`config/dbtenkaichi-common.json` (sincronizzato ai client) e `config/dbtenkaichi-client.json`, con
`schemaVersion`, backup automatici e migrazioni. Trasformazioni modificabili via datapack.
