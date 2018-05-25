# JAVA CLIENT for OPEN AI GYM HTTP SERVER
A simple java client to access the <a href="https://github.com/openai/gym-http-api">OpenAI Gym HTTP Server</a>. I tried looking for an existing java client, but the only one I found I couldn't get to work, so I decided to make one myself. 

This project is primarily focussed on simplicity, so I minimized reliance on dependencies (only 1 for JSON (in addition to some standard Java libs)), and tried to keep the code itself easy to understand and modify.

## Setup:
Clone or download this project (Import project from git if using Eclipse or other IDE).

Install this JSON jar: <a>http://central.maven.org/maven2/org/json/json/20180130/json-20180130.jar</a>.

Add that JSON jar to the project as an external archive (Build Path / Add External Archives in Eclipse).

See <a href="https://github.com/Ryan-Amaral/working-gym-java-client/blob/master/src/agent/SampleAgent.java">src/agent/SampleAgent.java</a> for an example.

You may have to delete previous run configurations before running the first time (if applicable to your setup).

## Todo:
Right now some api calls return ugly stuff, eg. ```resetEnv``` returns an ```Object``` which is probably a ```JSONArray```. This is because different environments may return different things. I will get around to create wrapper functions which extract the needed variables from such ```Objects``` automatically for common formats that may be seen. For now, it is not too difficult to deal with, look at the <a href="https://github.com/Ryan-Amaral/working-gym-java-client/blob/master/src/agent/SampleAgent.java">Sample Agent</a> for examples.
