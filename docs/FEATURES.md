# Funzionalità — Dragon Block: Tenkaichi 0.1.0 (vertical slice)

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
| Ki | Ki Blast con homing, Kamehameha con carica/sterzata/trincea | Implementata | smoke server + client |
| Ki | Beam Clash autoritativo con battiti, stagger, vittoria/pareggio | Implementata | non verificata in gioco |
| Mondo | Distruzione off/visual/real/regenerating, budget, protezioni | Implementata | smoke server (aria aperta) |
| Trasformazioni | Sistema data-driven + Super Saiyan (rabbia, cinematica, padronanza, drenaggio) | Implementata | client scriptato |
| NPC | Maestro: meditazione, controllo Ki, sparring, schivate, insegna il Kamehameha | Implementata | codice, verifica parziale |
| NPC | Predone Saiyan: FSM 10 stati, raffiche, cannone, schivate, seconda fase | Implementata | smoke server su entrambi i loader |
| UI | Interfaccia completamente personalizzata: pannelli e pulsanti a 9-slice, sfondo animato, intestazioni, barre inclinate, slider | Implementata | client scriptato (screenshot) |
| UI | HUD: ritratto del personaggio, BP, barre vita/Ki/stamina/fame con scia, armatura, icone delle tecniche, reticolo di lock-on; cuori/fame/armatura vanilla nascosti | Implementata | client scriptato (screenshot) |
| UI | Menu personaggio (statistiche/tecniche/forme/impostazioni), creator, allenamento con il nuovo stile | Implementata | client scriptato |
| Oggetti | Fagiolo Senzu, uova di NPC (texture 32×32 ombreggiate), tab creativa | Implementata | client scriptato (hotbar) |
| Lingue | Inglese e italiano (258 chiavi) | Implementata | script di controllo chiavi |
| Audio | 34 eventi, 50 suoni sintetizzati originali | Implementata | generazione verificata |

## Passive razziali

| Razza | Passive | Effetto in gioco |
|---|---|---|
| Saiyan | Zenkai, Fame di battaglia, Coda | Zenkai pieno; +50% guadagno di arti marziali colpendo avversari di forza simile o superiore |
| Mezzo-Saiyan | Zenkai, Potenziale nascosto | Zenkai al 60%; +25% danni sotto il 40% di HP |
| Umano | Maestria tecnica, Efficienza Ki | +25% pratica, +50% padronanza delle tecniche, Ki più economico |
| Namecciano | Rigenerazione, Arti allungabili | Rigenera 2%/s fuori combattimento pagando Ki; +1,2 blocchi di portata in mischia |
| Razza di Freezer | Sopravvivenza nello spazio, Coda | Ossigeno quasi illimitato, immune al congelamento |
| Majin | Rigenerazione, Corpo elastico, Assorbimento | Rigenera anche in combattimento; +40% resistenza al contraccolpo, -60% danni da caduta; uccidere cura e ricarica Ki |
| Androide | Riserva di energia, Non percepibile | Nessuna fatica da allenamento, rigenerazione del Ki doppia; i nemici non lo individuano oltre 12 blocchi |
| Bio-Androide | Rigenerazione, Assorbimento, Zenkai, Coda | Rigenerazione, assorbimento alle uccisioni e Zenkai |
| Kaioshin | Ki divino, Efficienza Ki | +15% potenza delle tecniche Ki |
| Razza demoniaca | Furia demoniaca | +30% danni sotto il 35% di HP; i colpi in mischia restituiscono Ki |

## Non ancora implementato (dal documento di progetto)
Trasformazioni per le razze non Saiyan e oltre il Super Saiyan, tecniche aggiuntive, fusioni, Sfere del
Drago e Shenron, dimensioni (Namek, Stanza dello Spirito e del Tempo, Aldilà...), strutture, viaggio
spaziale, story mode, boss canonici, tornei, gilde/party, musica dinamica, mesh/shader personalizzati
oltre alle pipeline vanilla, LOD dedicato, benchmark.
