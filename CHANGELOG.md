# Changelog

## [1.0.1](https://github.com/lentidas/DAI-2425-PW2/compare/v1.0.0...v1.0.1) (2024-12-05)


### Continuous Integration

* update release-please.yaml ([15fb6bd](https://github.com/lentidas/DAI-2425-PW2/commit/15fb6bdc150da7a940ee6d54bf0ab9c4ab3f2fa3))

## [1.0.0](https://github.com/lentidas/DAI-2425-PW2/compare/v0.1.0...v1.0.0) (2024-12-05)


### Features

* add client side commands as GameCommand classes ([289e356](https://github.com/lentidas/DAI-2425-PW2/commit/289e356230882a639c4ea0bff94a78cd61c7f2c7))
* add first prototype of commands and sockets ([0e3be56](https://github.com/lentidas/DAI-2425-PW2/commit/0e3be5674af378e10eb1ddf0bf33b00b87836cda))
* add first prototype of the picocli commands ([bc587e6](https://github.com/lentidas/DAI-2425-PW2/commit/bc587e60188e3e84dffc061cc076e2eb80031253))
* add flag to disable debug output ([5eca73d](https://github.com/lentidas/DAI-2425-PW2/commit/5eca73d372ef656c47b2ed2aa4f19b5d9d728558))
* add HELP and HOST commands ([17119d6](https://github.com/lentidas/DAI-2425-PW2/commit/17119d60124bae1e12f5ad1b9110e2831f1a3091))
* add support to use lower case commands ([04a5737](https://github.com/lentidas/DAI-2425-PW2/commit/04a573715f79d11a317cecc1c83a60fa79f2da38))
* Adding base class for commands ([098ef84](https://github.com/lentidas/DAI-2425-PW2/commit/098ef84c2ba31deeea6d2e2abba6bc02386d434a))
* adding player and game lobby classes ([f872837](https://github.com/lentidas/DAI-2425-PW2/commit/f872837211155a512316167050af8ace80c493da))
* adding puzzle class ([243081a](https://github.com/lentidas/DAI-2425-PW2/commit/243081a3e7e608623ab74b4e4fb89874cc58cc15))
* Adding remaining commands ([388d392](https://github.com/lentidas/DAI-2425-PW2/commit/388d3926d8f0fb2d56710cc4f85370bebc8c34fc))
* adding RFC-like document ([5716e50](https://github.com/lentidas/DAI-2425-PW2/commit/5716e5060f81654cef6380f6df4066e58f880362))
* **client:** first implementation with separate threads for in and out ([d995d33](https://github.com/lentidas/DAI-2425-PW2/commit/d995d332f55d1cd8e880d044a7b6412e2163e808))
* implement first working version of REPL server/client ([fc7137c](https://github.com/lentidas/DAI-2425-PW2/commit/fc7137c5b2616fd5cb357a2670b891f1f7ca5bb1))
* implement JOIN, GO, QUIT and a few STATUS and END responses ([6e42511](https://github.com/lentidas/DAI-2425-PW2/commit/6e4251153d33ad08ff19627c98e0ff7ed97b2944))
* implement output for the majority of commands and responses ([f53da6e](https://github.com/lentidas/DAI-2425-PW2/commit/f53da6e3efd31e8e2580b373645f379186b9c1b2))
* improve handling of exceptions and remove old code ([1286b7a](https://github.com/lentidas/DAI-2425-PW2/commit/1286b7ad0b283cc5bb697a13af3efa8552768715))
* improve management of IP address and port coming from the command ([273cd92](https://github.com/lentidas/DAI-2425-PW2/commit/273cd925cebf84bb153fce352e33afcc0104ee07))
* puzzles ([38201ae](https://github.com/lentidas/DAI-2425-PW2/commit/38201ae988a22650042bff1710fbc77051d3df73))
* rename executable in Dockerfile and add EXPOSE setting ([3d8f66d](https://github.com/lentidas/DAI-2425-PW2/commit/3d8f66d796352168b4cbef8b750ea807f7fe24d0))
* Starting to implement interfaces ([fdd6632](https://github.com/lentidas/DAI-2425-PW2/commit/fdd663276a30cfb15471d6fba648909578feda89))
* use the number of max players from GameMatch class ([0c57304](https://github.com/lentidas/DAI-2425-PW2/commit/0c57304d351a8af93ac070f7dfb680edb8a1d50e))


### Bug Fixes

* commands did not check for null arguments ([b736dd3](https://github.com/lentidas/DAI-2425-PW2/commit/b736dd34794ba8a63e563e018bd66b4e0fac0ae9))
* correct the syntax on the bind/host option description ([d25cae9](https://github.com/lentidas/DAI-2425-PW2/commit/d25cae9e5c8a5e5d2fafb0ce2fc82f604f784979))
* fixed spec problems ([5a57122](https://github.com/lentidas/DAI-2425-PW2/commit/5a571228bd740ef6b8b658c095b34c889280397f))
* fixing small issue with user disconnection ([7ce62cc](https://github.com/lentidas/DAI-2425-PW2/commit/7ce62cc8207f7e44913ab21605045068faf0d7c9))
* fixing start command parsing ([a179ad5](https://github.com/lentidas/DAI-2425-PW2/commit/a179ad53902db638d05c97832c8856593fbdd127))
* fixing the few commands that did not work correctly from the client side ([dd197d5](https://github.com/lentidas/DAI-2425-PW2/commit/dd197d57f1de49841bd33cbf6edc0a3ab59df2ac))
* lobby command was not being parsed ([0b51a80](https://github.com/lentidas/DAI-2425-PW2/commit/0b51a8090c40f2ed040707b728ce6f0c7acaffa7))
* move timeout constant outside the try block ([67288a2](https://github.com/lentidas/DAI-2425-PW2/commit/67288a2361689f67e398106e68888dcd8a1649da))
* new lines on Windows ([7eb8f4a](https://github.com/lentidas/DAI-2425-PW2/commit/7eb8f4aefbab0139a479187b12fd86a81c6aca0f))
* protect the user input readers from exceptions ([622f6a6](https://github.com/lentidas/DAI-2425-PW2/commit/622f6a62308c430c415ded0a1213879d7e88e48d))
* skip doesn't update player state ([f1a9f75](https://github.com/lentidas/DAI-2425-PW2/commit/f1a9f753c4056d9c82ebb8b18792d503594fc87d))
* small fixes in specs ([5840605](https://github.com/lentidas/DAI-2425-PW2/commit/58406058bd9e877b776574366149d650807be788))
* vowel guess skips turn ([f1a9f75](https://github.com/lentidas/DAI-2425-PW2/commit/f1a9f753c4056d9c82ebb8b18792d503594fc87d))
* wrong consonant guess does not update used letters ([f1a9f75](https://github.com/lentidas/DAI-2425-PW2/commit/f1a9f753c4056d9c82ebb8b18792d503594fc87d))


### Reverts

* reverted number of rounds ([e02291f](https://github.com/lentidas/DAI-2425-PW2/commit/e02291f17e12e5a5a07b9a76b2adf4953a49ff46))
* reverted server active wait ([a758792](https://github.com/lentidas/DAI-2425-PW2/commit/a7587923977d7ab0b3fecf768ea0e0a0177dd060))


### Documentation

* add examples using the JAR directly ([2019ce7](https://github.com/lentidas/DAI-2425-PW2/commit/2019ce7789428cacfc72f19a77a149c6b5ac2de9))
* add first draft of the README.md ([9d5070a](https://github.com/lentidas/DAI-2425-PW2/commit/9d5070afbebe2c09b0f3b559c1b235f560806087))
* add Javadoc comments to Player and Main classes ([4e4887a](https://github.com/lentidas/DAI-2425-PW2/commit/4e4887a337f839b5b9fb5d83510466637047a4d2))
* add Javadoc comments to the commands package ([ca7c18c](https://github.com/lentidas/DAI-2425-PW2/commit/ca7c18c6b8a4b19cb93f6fcccd09a19c8262b6bb))
* add Javadoc comments to the core classes of the logic package ([9aa76e3](https://github.com/lentidas/DAI-2425-PW2/commit/9aa76e3210881f7d1ac7a8355d5e04eff6573532))
* add Javadoc comments to the logic.commands package ([e52d81b](https://github.com/lentidas/DAI-2425-PW2/commit/e52d81b196f82c48aec5c459b37730a8a6ef9c12))
* add Javadoc comments to the network package ([aa860e2](https://github.com/lentidas/DAI-2425-PW2/commit/aa860e216b6a65f4e4f54b37e6ea6bce8b9f4986))
* add Javadoc comments to the puzzle package ([e4a56e8](https://github.com/lentidas/DAI-2425-PW2/commit/e4a56e892068c489448a3014d8f645cbcc95f939))
* add Javadoc comments to the wheel package ([107c9a1](https://github.com/lentidas/DAI-2425-PW2/commit/107c9a1ccdf88a7c2dcf943b0541716f33bbc59d))
* add protocol specification documents ([#8](https://github.com/lentidas/DAI-2425-PW2/issues/8)) ([da581e0](https://github.com/lentidas/DAI-2425-PW2/commit/da581e0c10b38f26c333fdc73efd4bc841c8fe43))
* add the remaining Docker commands to the README.md ([c583124](https://github.com/lentidas/DAI-2425-PW2/commit/c583124555e7906122afbb28aec302c287a74e53))
* adds the README and Javadocs ([46334b3](https://github.com/lentidas/DAI-2425-PW2/commit/46334b3947e7020264ad7a3480f2cee423a892d3))
* adds the README and Javadocs ([46334b3](https://github.com/lentidas/DAI-2425-PW2/commit/46334b3947e7020264ad7a3480f2cee423a892d3))
* First spec version ([387313d](https://github.com/lentidas/DAI-2425-PW2/commit/387313d51d2df006f5f7ab6d4217571fe4a57bf8))
* fix typo ([db88f6c](https://github.com/lentidas/DAI-2425-PW2/commit/db88f6c5e44948db3dcfc3fffaa61a0a4ce263ad))
* remove a few TODOs and add some others ([3552e39](https://github.com/lentidas/DAI-2425-PW2/commit/3552e39ca47cee8ec91c2694c6347d83c95205f1))
* remove a few TODOs and fix a few typos ([3795d21](https://github.com/lentidas/DAI-2425-PW2/commit/3795d2195b2f4e156276693fda7997d52bf84802))
* remove TODO from README ([7804b7c](https://github.com/lentidas/DAI-2425-PW2/commit/7804b7cae1c2f9b601f547dbab655f92c951038c))
* small rewording of the copyright message ([d8b3f4e](https://github.com/lentidas/DAI-2425-PW2/commit/d8b3f4e38d406845d739b8126c07ca25e76ded97))
* small rewording of the copyright message ([#9](https://github.com/lentidas/DAI-2425-PW2/issues/9)) ([29b3afc](https://github.com/lentidas/DAI-2425-PW2/commit/29b3afce1cdaa14bd134737a72cbaba8155ef968))
* small rewording of the copyright message ([#9](https://github.com/lentidas/DAI-2425-PW2/issues/9)) ([5a311fe](https://github.com/lentidas/DAI-2425-PW2/commit/5a311fee5d8f1077d90f82197d42bddcb0f8f6df))


### Styles

* rename executable ([565dabb](https://github.com/lentidas/DAI-2425-PW2/commit/565dabb40f69c92c744dbd0af750dbe89292d19e))


### Miscellaneous Chores

* add CallableInputReader class (probably to be deleted later) ([04aef4f](https://github.com/lentidas/DAI-2425-PW2/commit/04aef4f3c63af0da8a1a060414814fc87551def8))
* add new dependency and run configurations ([9c27aaf](https://github.com/lentidas/DAI-2425-PW2/commit/9c27aafa148c462767a81b6309457d5e1a6d2f83))
* adding interactive console classes ([e4b045e](https://github.com/lentidas/DAI-2425-PW2/commit/e4b045e251236a7fdef082a26302f7b5a87fa7d9))
* adding last round letter guessing ([5ab5914](https://github.com/lentidas/DAI-2425-PW2/commit/5ab59145ea52ae33662da0f9ca61265a42e7fddf))
* adding more docs ([218f4d6](https://github.com/lentidas/DAI-2425-PW2/commit/218f4d66c6a4e0c719c371cf20fa3fb6a1aab806))
* adding more logic to server ([3970258](https://github.com/lentidas/DAI-2425-PW2/commit/39702585b9c55ec9ba6dc2e29449edb78d695816))
* aligning socket server's max connections with game match's max players ([28dc3cc](https://github.com/lentidas/DAI-2425-PW2/commit/28dc3cc254802f93dff7ce1108d1da8d702417d2))
* applied spotless:apply ([18de913](https://github.com/lentidas/DAI-2425-PW2/commit/18de91335937dd091d1ef0eb5f5030cd9298fdbb))
* continuing interactive console implementation ([68171a5](https://github.com/lentidas/DAI-2425-PW2/commit/68171a59438dad8e23d524e64f4a8aa43739a0a4))
* delete .idea/remote-targets.xml ([df09d80](https://github.com/lentidas/DAI-2425-PW2/commit/df09d8001b4b0c7a3a7bd6e10bf64349d87db839))
* delete .idea/remote-targets.xml ([4a9933f](https://github.com/lentidas/DAI-2425-PW2/commit/4a9933f53a06c82c41a172ce65a5ca61e3fae082))
* fixing format for a command ([230c4d2](https://github.com/lentidas/DAI-2425-PW2/commit/230c4d2c6b1285f7d84731b45161c5d404b50c75))
* initial commit ([6086012](https://github.com/lentidas/DAI-2425-PW2/commit/608601200a8c824d6b1f7ceb4f0b011fb21b219e))
* making server not wait for client's answer if it's not their turn ([9888887](https://github.com/lentidas/DAI-2425-PW2/commit/9888887dccf039219bf7548f3b84f67128affe84))
* merge ([5ab1bf2](https://github.com/lentidas/DAI-2425-PW2/commit/5ab1bf29295f09e7dd7478daf7f17ecd3520b1c7))
* merge pull request [#10](https://github.com/lentidas/DAI-2425-PW2/issues/10) from lentidas/feat/interfaces ([0657c5e](https://github.com/lentidas/DAI-2425-PW2/commit/0657c5e9e94647664ef56d3193bfe279dc369e78))
* merge pull request [#13](https://github.com/lentidas/DAI-2425-PW2/issues/13) from lentidas/feat/server_logic ([29184a7](https://github.com/lentidas/DAI-2425-PW2/commit/29184a7811abf5bbb8c9d472edd3b0908b359dd6))
* merge pull request [#14](https://github.com/lentidas/DAI-2425-PW2/issues/14) from lentidas/feat/server_logic ([650ad74](https://github.com/lentidas/DAI-2425-PW2/commit/650ad74a591e3104eba3b105d7f685ec51f88ba9))
* merge pull request [#15](https://github.com/lentidas/DAI-2425-PW2/issues/15) from lentidas/feat/docs ([172edbe](https://github.com/lentidas/DAI-2425-PW2/commit/172edbe1d877dd5c17db08d6c8f696b276e9ee56))
* merge pull request [#16](https://github.com/lentidas/DAI-2425-PW2/issues/16) from lentidas/feat/server_logic ([c5a0cf7](https://github.com/lentidas/DAI-2425-PW2/commit/c5a0cf7bbdf737c751854db9f5968767d162bd53))
* new PDF ([3a60405](https://github.com/lentidas/DAI-2425-PW2/commit/3a604052b7fab6018998339227d87cd1ac9caea7))
* rebased main into branch ([b7431fd](https://github.com/lentidas/DAI-2425-PW2/commit/b7431fd61040e77c7718a89472a4b11b07a7bd45))
* remove TODO ([ab0977f](https://github.com/lentidas/DAI-2425-PW2/commit/ab0977f4748b3c55f4088899d636a33fa901c1f7))
* remove unused Interface ([dc4ed6a](https://github.com/lentidas/DAI-2425-PW2/commit/dc4ed6a887a6e5d18e387e4a2df6721b9859d707))
* server answers to a few requests ([71bdc79](https://github.com/lentidas/DAI-2425-PW2/commit/71bdc79eb7a2bab09e319224bee8c853a2c4fc90))
* server logic seems to be fully functional ([6a7f187](https://github.com/lentidas/DAI-2425-PW2/commit/6a7f18772198c93c1d94caf310a24a0726a8d250))
* server now answers most round commands from client ([ac99e0f](https://github.com/lentidas/DAI-2425-PW2/commit/ac99e0fd35cb8efac34a288a55b2499c157d2e24))
* spotless:apply ([f8f8141](https://github.com/lentidas/DAI-2425-PW2/commit/f8f814115774fbaee26ffb44227e35bae90db0c9))
* spotless:apply ([7add2cf](https://github.com/lentidas/DAI-2425-PW2/commit/7add2cf05f39976ceaf02e28333dbafd16fb70d3))
* trigger v1.0.0 release ([c4c2afb](https://github.com/lentidas/DAI-2425-PW2/commit/c4c2afb5bbed5ad75b6e512f7142bc9f58742149))
* updating RFC with more information ([38150bd](https://github.com/lentidas/DAI-2425-PW2/commit/38150bd2d97ab1a34501f88620fcb45757b36286))
* updating specs to include LETTERS command ([8675091](https://github.com/lentidas/DAI-2425-PW2/commit/86750914707970c26004845ce7290e7870b30800))


### Code Refactoring

* capitalize the constant variable names ([a1b38fd](https://github.com/lentidas/DAI-2425-PW2/commit/a1b38fd1f2f43c3a6c1bd77301e7af4af3d5c12f))
* simplify imports ([58a8d2b](https://github.com/lentidas/DAI-2425-PW2/commit/58a8d2bcbe9c4597207fabf2fa8b4c5759e4a3a4))


### Tests

* try using a simple ENTER to get new commands ([49a20b7](https://github.com/lentidas/DAI-2425-PW2/commit/49a20b778efc5374fa92646b06b618f4fb5c6d83))
