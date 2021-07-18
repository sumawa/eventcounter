# Event Counter

there are binaries for Mac/Windows/Linux - these binaries will print out JSON lines with attributes “event_type”, “data”, and “timestamp”.

## Challenge

- Create an application that 
	- reads the lines emitted by the process 
	- and performs a windowed (arbitrary duration left for you to choose) word count, grouped by event_type.

- The current word count should be exposed over an HTTP interface from your application.

- Note that the binaries sometimes output garbage data so you’ll need to handle that gracefully.

- The application should be written in Scala with your frameworks/libraries of choice (no need to learn something new for this challenge - use what you know).

