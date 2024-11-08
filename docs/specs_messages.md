# Messages

| Message   | Server $\rightarrow$ Client | Server $\leftarrow$ Client  | Description             |
|:----------|:------------:|:------------:|:------------------------------------------------------|
| `END`     | $\checkmark$ |              | Annonce de la fin du jeu, avec résultats              |
| `FILL`    |              | $\checkmark$ | Le joueur essaye de compléter le puzzle               |
| `GUESS`   |              | $\checkmark$ | Le joueur essaye devine une consonne                  |
| `INFO`    | $\checkmark$ |              | Envoit les toutes dernières informations de la manche |
| `LAST`    | $\checkmark$ |              | Demande au gagnant de deviner le puzzle               |
| `LOBBY`   | $\checkmark$ |              | Envoie la liste actuelle de joueurs dans la partie    |
| `JOIN`    |              | $\checkmark$ | Demande au serveur d'authoriser un joueur à rejoindre |
| `QUIT`    |              | $\checkmark$ | Demande la déconnection d'un joueur                   |
| `ROUND`   | $\checkmark$ |              | Fin de tour: le puzzle a été résolu                   |
| `STATUS`  | $\checkmark$ | $\checkmark$ | Renvoit un code de status à l'autre bout              |
| `TURN`    | $\checkmark$ |              | Le serveur tourne la roue pour un joueur              |
| `VOWEL`   |              | $\checkmark$ | Le joueur achète une voyelle                          |
| `WINNER`  | $\checkmark$ |              | Annonce le gagnant des manches. Début du dernier puzzle |

: Liste de tous les messages qui peuvent être échangés

## `END`

TODO

## `FILL`

TODO

## `GUESS`

TODO

## `INFO`

TODO

## `LOBBY`

TODO

## `JOIN`

TODO

## `QUIT`

TODO

## `ROUND`

TODO

## `START`

TODO

## `STATUS`

### Description

Un des clients fournit une réponse à une information venant du serveur, ou le serveur fournit une
réponse à une information venant du client. La liste des codes de réponse est connue par les deux
parties. Dans un projet plus conséquent, une vérification de version entre Serveur
$\leftrightarrow$ Client s'imposerait pour s'assurer que les deux ont des versions compatibles.
Toutefois, pour un laboratoire de cette taille, nous avons jugé ce point dénecessaire.

### Format, paramètres, réponses acceptées

| Nom                         | Description                                                       |
|:----------------------------|-------------------------------------------------------------------|
| Format                      | `STATUS <status>`                                                 |
| Server $\rightarrow$ Client | $\checkmark$                                                      |
| Client $\rightarrow$ Server | $\checkmark$                                                      |
| Réponse attendue            | Aucune                                                            |

: Tableau du format et réponse acceptées

#### Paramètres

| Nom    | Taille [B] | Description                                                               |
|:-------|:----------:|:--------------------------------------------------------------------------|
| Status | 2          | Code du status, au format hexadécimal majuscule                           |

: Paramètres de la commande `STATUS`

### Valeurs acceptées

Les valeurs marquées comme prioritaires doivent toujours exécuter le code lui appartenant, et
ensuite revenir à l'état précédent, à l'exception de `CLOSING` qui oblige le joueur à revenir à
l'état "Idle".

| Valeur | Nom              |    Prio.?    | Description                                                    |
|:------:|:----------------:|:------------:|:---------------------------------------------------------------|
| 0x00   | OK               |              | Réponse acceptée ou acceptable. Requête traitée correctement   |
| 0x01   | KO               |              | Erreur, mais l'erreur ne peut pas être spécifiée               |
| 0x02   | WRONG_FORMAT     |              | Le format de la commande reçue est faux ou incomplet           |
| 0x03   | PLAYER_JOINED    | $\checkmark$ | Un joueur a rejoint la partie. La commande `LOBBY` sera envoyée aussi |
| 0x04   | PLAYER_QUIT      | $\checkmark$ | Un joueur a quitté la partie. La commande `LOBBY` sera envoyée aussi |
| 0x05   | GAME_START       |              | La partie commence. Aucune autre connection n'est possible     |
| 0x06   | LETTER_EXISTS    |              | La lettre fournie existe. Le joueur peut continuer son tour    |
| 0x07   | LETTER_MISSING   |              | La lettre fournie n'existe pas. Le tour du joueur est terminé  |
| 0x08   | TIMEOUT          |              | Le temps est écoulé. Le tour du joueur est terminé             |
| 0x09   | NOT_YOU          |              | Un joueur a envoyé une commande lorsque ce n'était pas son tour |
| 0x0A   | WRONG_ANSWER     |              | La tentative de résolution du puzzle a échoué. Fin du tour     |
| 0x0B   | RIGHT_ANSWER     |              | La tentative de résolution du puzzle a réussi. Fin de la manche |
| 0x0C   | DUPLICATE_NAME   |              | Un autre joueur avec ce nom est déjà présent dans le lobby     |
| 0x0D   | CLOSING          | $\checkmark$ | Le serveur est en train de fermer. Tous les joueurs quittent   |
| 0x0E   | NO_FUNDS         |              | Le joueur n'a pas assez d'argent pour effectuer l'achat        |
| 0x0F   | SKIP             |              | Le joueur termine son tour sans effectuer d'action             |

: Valeur acceptées pour la commande `STATUS`


## `TURN`

TODO

## `VOWEL`

TODO

## `WINNER`

TODO

# Diagramme de séquence

## Connection d'un joueur

Un joueur envoie son username comme paramètre de la commande `JOIN`. Si la requête est acceptée par
le serveur, il répondra avec un `STATUS` acceptable. La commande `STATUS` est envoyée aux autres
joueurs dans le lobby pour les informer qu'un joueur a rejoint la partie. La command `LOBBY` est
ensuite envoyée à chacun des joueurs dans le lobby.

![Joueurs rejoignent la partie](./img/lobby_join.svg)

<!--
title Joueurs rejoignent le lobby

participant Joueur 1
participant Joueur 2
participant Server

Joueur 1->(1)Server:JOIN joueur_1
Server->(1)Joueur 1: STATUS ok
Server->(1)Joueur 1: LOBBY

Joueur 2->(1)Server:JOIN joueur_2
Server->(1)Joueur 2: STATUS ok
Server->(1)Joueur 1: STATUS joined
Server->(1)Joueur 2: LOBBY
Server->(1)Joueur 1: LOBBY
-->

## Tour d'un joueur

Le serveur informe un joueur que son tour a commencé. Il reçoit la somme d'argent obtenue, ainsi
que le puzzle à résoudre. À la fin de son tour, le serveur effectuera le même échange avec le
prochain participant dans la liste des joueurs.

![Tour d'un joueur](img/player_round.svg)

<!--
title Tour d'un joueur

participant Joueur 1
participant Joueur 2
participant Joueur 3
participant Server

Server->(1)Joueur 2: TURN <money>
Server->(1)Joueur 2: INFO
Joueur 2->(1)Server: GUESS <letter>
Server->(1)Joueur 2: STATUS letter_exists
Server->(1)Joueur 2: INFO
Joueur 2->(1)Server: VOWEL <letter>
Server->(1)Joueur 2: STATUS letter_missing
Server->(1)Joueur 3: TURN <money>
Server->(1)Joueur 3: INFO
-->

## Fin de manche

Lorsqu'un joueur devine correctement le puzzle, la fin de la manche est annoncée à chacun des
joueurs dans la partie.

![Fin de manche](img/round_end.svg)

<!--
title Fin de manche

participant Joueur 1
participant Joueur 2
participant Joueur 3
participant Server

Joueur 2->(1)Server: GUESS <letter>
Server->(1)Joueur 2: STATUS letter_exists
Server->(1)Joueur 2: INFO
Joueur 2->(1)Server: FILL <puzzle>
Server->(1)Joueur 2: STATUS wrong_answer
Server->(1)Joueur 3: TURN <money>
Server->(1)Joueur 3: INFO
Joueur 3->(1)Server: GUESS <letter>
Server->(1)Joueur 3: STATUS letter_exists
Server->(1)Joueur 3: INFO
Joueur 3->(1)Server: FILL <puzzle>
Server->(1)Joueur 3: STATUS right_answer
Server->(1)Joueur 1: ROUND
Server->(1)Joueur 2: ROUND
Server->(1)Joueur 3: ROUND
-->


## Fin du jeu

Lors de la dernière partie du jeu, le gagnant de toutes les manches précédentes est annoncé, et
doit trouver le puzzle qui lui est affiché à l'écran. S'il parvient à la résoudre correctement dans
le temps qui lui a été imparti, il gagne le jeu. Même s'il ne le gagne pas, tous les joueurs
reçoivent le résultat du jeu.

![Début du dernier puzzle, fin du jeu](img/game_end.svg)

<!--
title Début du dernier puzzle, fin du jeu

participant Joueur 1
participant Joueur 2
participant Joueur 3
participant Server

Server->(1)Joueur 3: STATUS right_answer
Server->(1)Joueur 1: ROUND
Server->(1)Joueur 2: ROUND
Server->(1)Joueur 3: ROUND
Server->(1)Joueur 1: WINNER
Server->(1)Joueur 2: WINNER
Server->(1)Joueur 3: WINNER
Server->(1)Joueur 2: STATUS last_round
Joueur 2->(1)Server: FILL <puzzle>
Server->(1)Joueur 2: STATUS right_answer
Server->(1)Joueur 1: END
Server->(1)Joueur 2: END
Server->(1)Joueur 3: END
-->

# Lifecycles

## Client

TODO

## Server

TODO
