# working-gym-java-client
A simple java client to access the OpenAI Gym HTTP Server. I tried looking for an existing java client, but the only one I found I couldn't get to work, so I decided to make one myself. 

This project is primarily focussed on simplicity, so I minimized any dependencies (only 1 for JSON), and tried to keep the code itself easy to understand and modify.

## Setup:
Clone or download this project (Import project from git if using Eclipse or other IDE).
Install this JSON jar: <a>http://central.maven.org/maven2/org/json/json/20180130/json-20180130.jar</a>.
Add that JSON jar to the project as an external archive (Build Path / Add External Archives in Eclipse).
See src/agent/SampleAgent.java for an example.
You may have to delete previous run configurations before running the first time (if applicable to your setup).