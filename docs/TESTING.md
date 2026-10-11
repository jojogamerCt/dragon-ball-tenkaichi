# Testing — Dragon Ball: Tenkaichi

Tutte le prove sotto sono state eseguite davvero l'11 ottobre 2026 su Windows 11, Java 25.0.1,
Minecraft 26.3, Fabric Loader 0.19.5 + Fabric API 0.162.0+26.3, NeoForge 26.3.0.71-beta.

## 1. Build pulita (entrambi i loader)

```bash
./scripts/gw.sh clean build
```

Esito: **BUILD SUCCESSFUL**, 151 classi per JAR, asset/dati/mixin/servizi presenti in entrambi:
`build/release/dragon-ball-tenkaichi-0.1.0-26.3-fabric.jar`, `...-neoforge.jar`.

## 2. Test unitari (JUnit 5, modulo common)

```bash
./scripts/gw.sh :common:test
```

19/19 superati (`BalanceTest`): monotonicità del BP, range di partenza e fine gioco, condizione e
soppressione, limiti del fattore di potenza, "un combattente più debole può vincere con la tecnica",
perfect guard, combo scaling, compressione dei moltiplicatori di forma, costi superlineari,
convergenza dell'allenamento ripetuto (anti-grinding), penalità di fatica, ricompense anti-farm,
cap delle statistiche, Zenkai, razze meccanicamente distinte.

## 3. Smoke test su server dedicato

```bash
./scripts/gw.sh :fabric:runServer -PdbtSmoke
./scripts/gw.sh :neoforge:runServer -PdbtSmoke
```

Il server si avvia fino a `Done`, applica tutti i mixin comuni, carica il chunk (0,0) forzato,
genera Maestro, Predone e un bersaglio, lascia lavorare l'IA per 400 tick, spara un Kamehameha,
fa esplodere un colpo all'aperto, poi si arresta. Esito su **entrambi i loader: RESULT PASS**
(forme da datapack, 4 tipi di danno, suoni, oggetti, tab, entità; l'IA ha visitato
IDLE→APPROACH→COMBO (Fabric) e IDLE→APPROACH→COMBO→GUARD→ENRAGE (NeoForge) e ha colpito il
bersaglio con danni della mod).

Nota: richiede `runs/server/eula.txt` (accettata dal proprietario del progetto per lo sviluppo).

## 4. Test client scriptato con screenshot

Preparazione: copiare il mondo del server di test in `runs/client/saves/dbt_test` e usare
`runs/client/options.txt` con `pauseOnLostFocus:false`.

```bash
./scripts/gw.sh :fabric:runClient -PdbtSmoke
./scripts/gw.sh :neoforge:runClient -PdbtSmoke
```

Il client entra nel mondo con Quick Play ed esegue `ClientSmokeTest`: apertura automatica del
creator al primo accesso, creazione Saiyan via pacchetto, comandi `/tenkaichi`, carica del Ki,
volo, trasformazione Super Saiyan con cinematica, NPC, lock-on, carica e sparo del Kamehameha,
contro-raggio del Predone → **Beam Clash** sincronizzato, combo in posizione di combattimento,
menu personaggio; salva screenshot in `runs/client/screenshots/dbt_*.png` e chiude il gioco.

Esito: **Fabric e NeoForge completano tutti i passaggi** (character created: true, flying: true,
form: super_saiyan, beam clash active: true). Nessuna eccezione di rendering registrata
(`RenderGuard`). Screenshot significativi archiviati in `docs/screenshots/`.

## 5. Verificato visivamente (dagli screenshot)

- Editor del personaggio con anteprima 3D, skin procedurale, capelli e coda.
- HUD (HP, Ki, stamina, BP, TP, padronanza, slot tecniche, lock-on, barra del clash, letterbox).
- Aura in carica e aura Super Saiyan; capelli dorati e occhi verdi in forma Super Saiyan.
- Kamehameha (raggio multistrato con esplosioni lungo la traiettoria) e Beam Clash.
- Raggio viola del Predone, Maestro con gi arancione, menu statistiche.

## 6. Non ancora verificato

- Gioco con due client reali sullo stesso server dedicato (sincronizzazione tra giocatori distinti).
- Riconnessione durante una trasformazione/volo/clash, cambio di dimensione, morte durante il clash.
- Parata, perfect block, schivata, vanish, dash e recupero aereo con input umano reale.
- Sessioni di allenamento (meditazione, controllo Ki, sparring, schivate) e apprendimento del Kamehameha dal Maestro.
- Sblocco del Super Saiyan per rabbia e Zenkai in condizioni di gioco naturali.
- JAR di release in un'installazione di produzione (launcher ufficiale) invece dell'ambiente di sviluppo.
- Prestazioni (FPS, MSPT, RAM) e benchmark: non misurati.
