# Funzionalità — Dragon Block: Tenkaichi 0.3.0

Legenda: **Implementata** = codice + risorse + registrazioni + build verde su entrambi i loader;
la colonna "Verifica" indica come è stata provata (vedi `TESTING.md`).

| Area | Funzionalità | Stato | Verifica |
|---|---|---|---|
| Base | Build multi-loader, due JAR separati | Implementata | build |
| Base | Config comune/client versionate con migrazione e backup | Implementata | server dedicato |
| Base | Comandi `/tenkaichi stats|setstat|givetp|reset|unlockform|learn|restore|reload`; `setstat <giocatore> all <valore>` imposta tutte le statistiche | Implementata | client scriptato (singola stat), codice (all) |
| Personaggio | Editor con 3 schede, anteprima 3D, rotazione/zoom, casuale | Implementata | client scriptato (screenshot) |
| Personaggio | 10 razze giocabili: Saiyan, Mezzo-Saiyan, Umano, Namecciano, Razza di Freezer, Majin, Androide, Bio-Androide, Kaioshin, Razza del Regno Demoniaco | Implementata | JUnit + client (galleria) |
| Personaggio | Passive razziali con effetto reale lato server (vedi tabella sotto) | Implementata | JUnit (distinzione), codice; effetti non provati in gioco |
| Personaggio | Aspetto predefinito, palette della pelle e "colore dettagli" diversi per razza | Implementata | client scriptato |
| Personaggio | Modelli 3D razziali: antenne e orecchie (Namecciano), aspetto della prima forma (pelle rosa-lilla, cupola, guance, spalline e placca pettorale bianche, gemme colorate, corna scure curve verso l'alto, coda spessa) per la Razza di Freezer, antenna (Majin), aspetto stile Cell forma imperfetta per il Bio-Androide (pelle verde maculata, viso pallido con segni viola, bocca a becco, cresta nera a corona, esoscheletro segmentato, ali-guscio, coda a pungiglione), auricolari (Androide), orecchie a punta e orecchini (Kaioshin), corna e orecchie (Demoniaca) | Implementata | client scriptato (screenshot) |
| Personaggio | Skin procedurale HD 128×128: ombreggiatura, muscoli, viso dettagliato (palpebre, iridi, riflessi, naso, orecchie), abiti 3D sui layer esterni con pieghe, cintura, emblema, stivali; dettagli razziali (bande sulle braccia, macchie, placche) | Implementata | client scriptato |
| Personaggio | Armatura con spalline e pettorale 3D; barba e baffi del Maestro | Implementata | client scriptato |
| Personaggio | Outfit "Naturale": corpo con corazza biologica per la Razza di Freezer e per il Bio-Androide, pantaloncini per le altre razze | Implementata | client scriptato (galleria) |
| Personaggio | 7 acconciature selezionabili + 3 di trasformazione con ciocche curve a 4 segmenti, texture capelli HD; coda Saiyan a 8 segmenti animata | Implementata | client scriptato |
| Personaggio | Altezza (attributo `scale`) e corporatura (larghezza modello) | Implementata | client scriptato |
| Statistiche | 12 statistiche, TP, costo superlineare, cap da Potenziale, pratica | Implementata | JUnit |
| Statistiche | Battle Power con forma/Ki/salute/fatica/soppressione | Implementata | JUnit |
| Progressione | Rendimenti decrescenti giornalieri, fatica, ricompense combattimento anti-farm | Implementata | JUnit |
| Progressione | Zenkai per Saiyan dopo quasi-morte contro nemici più forti | Implementata | non verificata in gioco |
| Combattimento | Combo, pesanti contestuali, counter, rush, guard break, clash corpo a corpo | Implementata | smoke server (IA) + client |
| Combattimento | Parata direzionale, perfect block, stamina di guardia | Implementata | codice, non verificata in gioco |
| Combattimento | Schivata i-frame, vanish, teleport alle spalle, dash, recupero aereo | Implementata | codice, non verificata in gioco |
| Combattimento | Hitstop, scossa camera, particelle, suoni d'impatto | Implementata | client scriptato |
| Combattimento | Lock-on con cambio bersaglio, reticolo, rilascio automatico | Implementata | client scriptato |
| Combattimento | Braccia visibili in prima persona in posizione di combattimento: guardia, pugni alternati, montante, parata | Implementata | client scriptato (screenshot) |
| Combattimento | Combo breaker: dopo 3 colpi consecutivi di un NPC il giocatore è immune allo stordimento per 2 s; stordimento dei colpi NPC ridotto | Implementata | codice |
| Volo | Hover/normale/supersonico, inerzia, bang sonico, atterraggio con onda d'urto | Implementata | client scriptato |
| Ki | Carica, rigenerazione, esaurimento, Ki/stamina/BP in HUD | Implementata | client scriptato |
| Ki | Aura 3D procedurale con fiamme a bordi sfumati e sagoma a goccia (senza guscio a cubi), 4 livelli di qualità | Implementata | client scriptato |
| Effetti | 8 particelle custom (scintille Ki, fiamme d'aura, sfere di energia, stelle d'impatto, onde d'urto, polvere, fulmini, linee di velocità) per colpi, parate, schivate, aura, trasformazioni, carica e raggio della Kamehameha, esplosioni, clash | Implementata | client scriptato |
| Ki | Kamehameha sempre blu, indipendentemente dal colore dell'aura del giocatore | Implementata | client scriptato |
| Ki | Posa iconica di carica della Kamehameha: tutto il corpo ruotato di lato con la testa verso il bersaglio, braccia davanti al busto con le mani unite sul fianco destro, gambe larghe, tremito; mani a coppa in prima persona | Implementata | client scriptato (4 viste + prima persona) |
| Trasformazioni | Cutscene data-driven per forma (campo `cinematic` nel JSON): regia con inquadrature e movimenti di camera, fulmini, rocce che levitano, oscuramento del cielo, lampi a schermo, capelli che lampeggiano, colonna d'aura, suoni sincronizzati | Implementata | client scriptato (7 screenshot della cutscene) |
| Trasformazioni | Tre cutscene distinte: `super_saiyan_awakening` (ispirata alla prima trasformazione su Namek, ~7,5 s), `dark_rage` (Predone), `power_up` (predefinita per le forme future) | Implementata | Super Saiyan provata; `dark_rage` e `power_up` solo codice |
| Ki | Ki Blast con homing, Kamehameha con carica/sterzata/trincea | Implementata | smoke server + client |
| Ki | Beam Clash autoritativo con battiti, stagger, vittoria/pareggio | Implementata | non verificata in gioco |
| Mondo | Distruzione off/visual/real/regenerating, raggio proporzionale al BP con tetto, lavoro distribuito su più tick, crateri (Big Bang Attack), protezioni | Implementata | smoke server (cratere, limiti) |
| Zone sicure | Luoghi iconici indistruttibili (giocatori, tecniche, TNT/creeper), nessuno spawn di mob tranne i Maestri, migrazione dei mondi vecchi con ricostruzione | Implementata | smoke server (protezione, spawn, migrazione) |
| Trasformazioni | Sistema data-driven + Super Saiyan (rabbia, cinematica, padronanza, drenaggio) | Implementata | client scriptato |
| NPC | Maestro: meditazione, controllo Ki, sparring, schivate, insegna il Kamehameha | Implementata | codice, verifica parziale |
| NPC | Predone Saiyan: FSM 10 stati, raffiche, cannone, schivate, seconda fase | Implementata | smoke server su entrambi i loader |
| UI | Interfaccia completamente personalizzata: pannelli e pulsanti a 9-slice, sfondo animato, intestazioni, barre inclinate, slider | Implementata | client scriptato (screenshot) |
| UI | HUD: ritratto del personaggio, BP, barre vita/Ki/stamina/fame con scia, armatura, icone delle tecniche, reticolo di lock-on; cuori/fame/armatura vanilla nascosti | Implementata | client scriptato (screenshot) |
| UI | Menu personaggio (statistiche/tecniche/forme/impostazioni), creator, allenamento con il nuovo stile | Implementata | client scriptato |
| Oggetti | Fagiolo Senzu, uova di NPC (texture 32×32 ombreggiate), tab creativa | Implementata | client scriptato (hotbar) |
| Lingue | Inglese e italiano (258 chiavi) | Implementata | script di controllo chiavi |
| Audio | 36 eventi, 70 suoni sintetizzati originali con sintesi stratificata (filtri risonanti, FM, supersaw, crepitii granulari, riverbero a convoluzione, voce con formanti per l'urlo, tuoni, boati) | Implementata | generazione e analisi spettrale verificate; ascolto non verificabile dall'autore |

## Passive razziali

| Razza | Passive | Effetto in gioco |
|---|---|---|
| Saiyan | Zenkai, Fame di battaglia, Coda | Zenkai pieno; +50% guadagno di arti marziali colpendo avversari di forza simile o superiore; coda avvolgibile, afferrabile, tagliabile (ricresce), Grande Scimmia con luna piena o Power Ball |
| Mezzo-Saiyan | Zenkai, Potenziale nascosto | Zenkai al 60%; +25% danni sotto il 40% di HP |
| Umano | Maestria tecnica, Efficienza Ki | +25% pratica, +50% padronanza delle tecniche, Ki più economico |
| Namecciano | Rigenerazione, Arti allungabili | Rigenera 2%/s fuori combattimento pagando Ki; +1,2 blocchi di portata in mischia |
| Razza di Freezer | Sopravvivenza nello spazio, Coda | Ossigeno quasi illimitato, immune al congelamento |
| Majin | Rigenerazione, Corpo elastico, Assorbimento | Rigenera anche in combattimento; +40% resistenza al contraccolpo, -60% danni da caduta; uccidere cura e ricarica Ki |
| Androide | Riserva di energia, Non percepibile | Nessuna fatica da allenamento, rigenerazione del Ki doppia; i nemici non lo individuano oltre 12 blocchi e la percezione del ki/gli scouter non lo vedono oltre 12 blocchi |
| Bio-Androide | Rigenerazione, Assorbimento, Zenkai, Coda | Rigenerazione, assorbimento alle uccisioni e Zenkai |
| Kaioshin | Ki divino, Efficienza Ki | +15% potenza delle tecniche Ki; ki divino invisibile alla percezione dei mortali e agli scouter |
| Razza demoniaca | Furia demoniaca | +30% danni sotto il 35% di HP; i colpi in mischia restituiscono Ki |

## Novità della 0.2.0
- **Sblocco delle trasformazioni**: Potenziale (SS 8, SS2 20, SS3 35), BP base (20.000, 90.000, 250.000) e prova contro se stessi nella forma;
  pulsante Prova e requisiti dettagliati nella scheda Forme del menu U
  (Namecc, ring del Cell Game, canyon); arene di ogni prova in posizioni separate e ring da cui non si esce.
- **Super Saiyan 2 e 3** con cutscene canoniche
  abbreviate (Gohan contro Cell perfetto, Goku contro Majin Bu).
- **Kaioken** per tutte le razze, separato dalle forme e combinabile con quelle compatibili (base e Super Saiyan,
  campo datapack `kaiokenCompatible`): livelli x2, x3, x4, x10, x20 sbloccati dalla padronanza, tasto Y, consuma Ki
  e salute, aura rossa e indicatore nell’HUD. Si compra dalla lista tecniche (600 TP, BP base 4.000).
- **Tecniche**: Galick Gun, Masenko, Makankosappo, Final Flash, Big Bang Attack, Raggio della Morte. Ogni tecnica ha
  una posa canonica in terza e prima persona (`SkillStyle`, sincronizzato nei bit 17-20 dei flag di stato) e
  particelle di carica dove sono le mani. Si imparano comprandole o dai Maestri, non più in automatico.
- **Maestri canonici** (Maestro Muten, Karin, il Supremo) con lezioni: acquisto con TP o **prova** in stile
  Xenoverse 2 nella dimensione `dbtenkaichi:training_realm` (arena tematica, conto alla rovescia, combattimento a
  tempo, ritorno nel punto esatto di partenza anche dopo una disconnessione).
- **Luoghi iconici** generati una volta per mondo con il loro Maestro: Kame House nell’oceano più vicino, Torre di
  Karin con il santuario a disco, Palazzo del Supremo sopra la torre. `/tenkaichi landmarks` mostra le coordinate.
- Aura riprogettata: fiamme rivolte alla camera, più opache al centro del corpo.
- **Acconciature canoniche**: Goku, Vegeta, Gohan, Trunks del futuro, Yamcha, Radish, calvo (Crilin, Tensing), C-18,
  Videl e Chi-Chi; le trasformazioni cambiano i capelli in base all’acconciatura (`HairStyle.transformed`).
- **Outfit come capi indossabili** (gi, armatura da battaglia, casual: maglia, pantaloni, stivali) consegnati alla
  creazione; colore principale tingibile, colore dei dettagli salvato nell’oggetto; disegnati da `OutfitLayer` con una
  texture separata dalla pelle (anche sulle braccia in prima persona).

## Novità della 0.3.0
- **Modalità storia**: l'intera saga dei Saiyan in 10 missioni (`mission/Missions.java`) con il giocatore al posto di Goku
  e Gohan come allievo; 17 scene scriptate (`story/Scenes.java`, DSL `StoryScene`) con inquadrature, effetti e
  sottotitoli EN/IT (`tools/gen_story_lang.py`), saltabili con Invio solo se già viste; regia lato server
  (`SceneDirector`) e riproduzione lato client (`ScenePlayer`, camera dedicata e input bloccato).
- **Personaggi della storia** (`StoryNpc`, `StoryCharacters`): attori, nemici con soglia di sconfitta e alleati, con
  forma (anche Grande Scimmia), coda, scouter e tecnica distintiva.
- **Aldilà** (`dbtenkaichi:other_world`): ufficio di Re Enma, Via del Serpente (900 blocchi, chi cade torna sulla
  strada) e pianeta di Re Kaio con gravità 10x fino al termine dell'allenamento; aureola da morto.
- **Missioni** principali e secondarie (`MissionManager`, stato persistente in `MissionState`): menu O, HUD a destra con
  obiettivo, avanzamento e distanza, indicatore nel mondo con frecce ai bordi (disattivabile).
- **Percezione del ki** (tasto I) e **scouter**: marcatori a schermo con forza relativa o numeri; androidi e ki divino
  nascosti; lo scouter esplode oltre i 5 miliardi e reagisce sopra 9000.
- **Coda Saiyan e Grande Scimmia** (`TailSystem`), **Power Ball** (`PowerBallEntity`), **assorbimento** per Majin e
  Bio-Androidi (sblocca le forme da assorbimento).
- **Tutte le trasformazioni canoniche** per razza (43, dati in `tenkaichi_forms/` generati da `tools/gen_forms.py`) con
  passive aggiuntive (`FormPassive`: velocità, colpi, potenza ki, difesa, efficienza, schivata automatica, ki divino,
  gigante, furia, rigenerazione, sete di battaglia, ultima risorsa, riflesso, super armatura, distruzione, stamina
  infinita, intimidazione, rigenerazione ki, legata alla coda/luna), forme ramificate con Obiettivo, arene di prova
  tematiche nuove e una cutscene per ogni forma (`client/cinematic/FormCinematics.java`).
- **Tecniche canoniche per razza** (36 nuove): raggi, sfere giganti caricate sopra la testa, dischi, laser, raffiche e
  tecniche di supporto (`TechniqueEffects`), con pose e animazioni dedicate.
- Rimossa l'opzione **Voce** dal creatore del personaggio.

## Non ancora implementato (dal documento di progetto)
Fusioni, Sfere del Drago e Shenron, Namek e altre dimensioni, viaggio spaziale, saghe successive a quella dei Saiyan,
boss canonici fuori dalla storia, tornei, gilde/party, musica dinamica, mesh/shader personalizzati
oltre alle pipeline vanilla, LOD dedicato, benchmark.
