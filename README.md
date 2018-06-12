# pokemonshowdownbot
This bot is pretty poor, it will lose against an advanced player, unless rng in terms of team it uses or in terms of status, misses, ect.  So I'd like ideas and improvements to make this bot a lot better.  In random battles it appears to win 50% of its matches, presumably because it'll face better opponents then lose until it faces more novice opponents.

Download the jar in the target folder then run it with (might work by just opening the file from the file manager): 

`java -jar pokemon-showdown-bot-1.0-SNAPSHOT.jar`

This command will start a bot that you can challenge

This bot will auto accept all challenges

Accepts a property file path as an argument to the jar

you can assign you own `testclient.html` with the property: `testclient.path`

You can assign where it downloads the github repo with: `dest.dir`

You can change the github repo with: `git.url`

And change the branch with: `branch.name`

if `find.random` property is set to true then it will randomly find and fight random users

`username` and `password` property allows you to set the username and password for the bot

`username=${uuid}` will generate a random uid for the name

`challenge.user` allows you to select a user for it to auto challenge

`challenge.user` property is useful if you want it to fight another bot that accepts all challenges then you can watch the two
bots battle it out

### For the future
 - Bot improvements to be a stronger ai
 - Leaderboard which will be saved in this repository for anyone who downloads and fights this bot.
 Which will list which repository it fought against if that is changed.
 - Gym leaders, campaign mode.
