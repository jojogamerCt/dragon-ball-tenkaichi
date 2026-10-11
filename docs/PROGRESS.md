# Progress — Dragon Ball: Tenkaichi

## Sessione 1 — 11 ottobre 2026

### Implementato
- Repository multi-loader (common/fabric/neoforge/build-logic/tools/scripts/docs) su toolchain
  verificata per Minecraft 26.3; due JAR di release con nome `dragon-ball-tenkaichi-<ver>-26.3-<loader>.jar`.
- Seam piattaforma via ServiceLoader (registrazioni, rete, allegati persistenti, permessi blocchi).
- Dati giocatore persistenti e versionati; stato runtime autoritativo; sincronizzazione differenziale.
- Statistiche (12), Battle Power, progressione anti-grinding, fatica, pratica, Zenkai.
- Combattimento: combo, colpi pesanti contestuali, counter, rush, guard break, clash corpo a corpo,
  parata/perfect block, schivata/vanish/teleport, dash, knockdown, recupero aereo, lock-on.
- Volo predetto lato client con validazione server; atterraggi con onda d'urto; bang sonico.
- Ki: carica, rigenerazione, esaurimento; aura 3D; Ki Blast; Kamehameha; Raider Cannon; Beam Clash.
- Distruzione ambientale a 4 modalità con budget e protezioni.
- Trasformazioni da datapack; Super Saiyan con sblocco per rabbia, cinematica, padronanza.
- NPC: Maestro (allenamenti, insegnamento) e Predone Saiyan (FSM gerarchica, 10 stati, seconda fase).
- Client: editor personaggio, skin procedurale, 10 acconciature 3D, coda, animazioni a keyframe,
  HUD, menu personaggio con impostazioni, schermata di allenamento con minigiochi, effetti di
  combattimento (hitstop, scossa camera, particelle), mixin di input.
- Asset originali generati da script: 257 chiavi EN/IT, 50 suoni OGG (34 eventi), texture.
- Comandi `/tenkaichi`, configurazioni comune/client.

### Testato
- Build pulita di entrambi i loader, 19/19 test JUnit.
- Smoke test su server dedicato Fabric e NeoForge: PASS.
- Test client scriptato con screenshot su Fabric e NeoForge: tutti i passaggi completati.
- Dettagli e limiti in `TESTING.md`.

### Problemi trovati e risolti durante la sessione
- Certificati HTTPS intercettati dall'antivirus → wrapper Gradle con trust store di Windows.
- `CreativeModeTab.Output` protetto nel common → access transformer (i loader lo rendono già pubblico).
- Mixin anti-kick non applicabile su Fabric → riscritto su `tick()`.
- Smoke test che non faceva "tickare" le entità senza giocatori → chunk forzato e bersaglio persistente.
- Aura troppo opaca, layout HUD sovrapposto, testo del creator fuori schermo, sfera del clash troppo
  grande → corretti e riverificati con nuovi screenshot.

### Mancante (prossimi passi consigliati)
1. Test multiplayer con due client reali e prove di riconnessione/cambio dimensione.
2. Benchmark (FPS/MSPT) e ottimizzazione di aura/raggi.
3. Altre tecniche (Galick Gun, Masenko, Destructo Disc, Special Beam Cannon) e Super Saiyan 2/3.
4. Spawn naturale del Predone e strutture iniziali; trasformazioni per le razze non Saiyan.
5. Fasi 7–10 della roadmap.

### Verifiche fallite ancora aperte
Nessuna verifica automatica fallisce allo stato attuale.

## Sessione 2 — 11 ottobre 2026

### Implementato
- Tutte le 10 razze abilitate e giocabili (migrazione della config comune allo schema 2).
- `RaceMechanics`: ogni passiva razziale ha un effetto server-side (rigenerazione pagata in Ki,
  assorbimento alle uccisioni, portata allungata, corpo elastico, sopravvivenza nello spazio,
  invisibilità ai sensi dei nemici, furia demoniaca, Ki divino, fame di battaglia, maestria tecnica,
  riserva di energia, potenziale nascosto).
- Aspetto per razza: colori predefiniti, palette della pelle, "colore dettagli" sincronizzato in rete.
- Skin procedurali HD 128×128 con abiti 3D sui layer esterni; dettagli razziali sulla skin.
- Nuovi modelli: tratti razziali (3 gruppi di colore), coda spessa, spalline dell'armatura, barba;
  ciocche dei capelli curve a 4 segmenti; coda Saiyan a 8 segmenti; nuove texture HD.
- Nuova icona della mod fornita dal proprietario del progetto.

### Testato
- Build pulita, 19/19 JUnit; smoke server Fabric e NeoForge: PASS.
- Test client su entrambi i loader con galleria delle 10 razze: tutti i passaggi completati.

### Problemi trovati e risolti
- Il creator sostituito da un altro creator azzerava l'anteprima (`removed()` dopo il costruttore)
  → l'anteprima viene ripubblicata in `init()`.
