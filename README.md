# Carpe Diem 鯉

Un petit jeu de détente dans le navigateur : un bassin à koï et un jardin de sable, sans but ni score.
Pastel, minimaliste, inspiré des estampes japonaises. Le jeu tient dans un seul fichier, `index.html`, sans dépendance à installer.

## Jouer
En ligne : https://maisonparadoxe.github.io/carpediem/ (GitHub Pages, branche `main`, dossier racine).
En local : ouvre `index.html` dans un navigateur.
La progression est sauvegardée dans le navigateur (`localStorage`), propre à chaque adresse et à chaque appareil.

## Ce qu'on y fait
- **Bassin :** nourrir (3 nourritures, chacune préférée par certaines espèces), nettoyer, poser et déplacer des décors, accueillir, vendre ou libérer des poissons.
- **Poissons :** chacun a un nom, un caractère, une humeur et une taille qui évolue. Des croisements donnent des espèces rares (Yamabuki, Asagi, Showa, Ginrin, Purachina, Shubunkin). Cinq poissons légendaires se méritent avec le temps (Taiyō, Yoru, Ame, Hasu, Ryū). Un carnet d'espèces garde la trace de tes découvertes.
- **Jardin zen :** à débloquer avec des pièces. On y trace au doigt ou au râteau, on pose, déplace et retire des rochers. Les traces s'estompent lentement, la pluie mouille le sable et y laisse de petits creux.
- **Ciel :** l'heure et la météo (soleil, nuages, brume, pluie) varient lentement au hasard. Réglable dans « ··· » (heure réelle, météo fixe).
- **Ambiance :** sons d'eau discrets, un souffle de vent de temps en temps, phrases zen à l'encre avec leur auteur, libellule de passage.
- **Mode fond d'écran :** masque l'interface (trois touches pour la retrouver) ; mode économe à 30 images par seconde.

## Notes techniques
- Canvas 2D, Web Audio, `localStorage` (clés `carpe-diem-v1` et `carpe-diem-sand-v1`). La progression se calcule en temps réel, même quand la page est fermée.
- Polices Google Fonts : Shippori Mincho, Zen Maru Gothic, Yuji Boku.
- La météo est simulée (la vraie météo locale demanderait une version autonome avec accès réseau).

## Idées pour la suite
Héron, saisons plus marquées, version fond d'écran avec la vraie météo, plus d'outils pour le jardin.
