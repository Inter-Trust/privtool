# privtool
## Privacy Preserving Tool

### Implemented functions:
- Approximation to N decimal digits (Approx Function).
- Resolution modification of the position (Reslos Function).
- Gaussian Noise Addition with custom mean and variance. (Noise Function).

### Execution Mode:
- Execute in command line: java -jar intertrust.jar -function parameters.
- Examples:
	- java -jar intertrust.jar -reslos 12.034 23.434 0.5
	- java -jar intertrust.jar -approx 12.034 23.434 2
	- java -jar intertrust.jar -noise 2 1.5 -in database.txt

### Usage Parameters:
- -help: Show this usage.
- -reslos: Modifies resolution of the position. {pos1 pos2 ... posN res}.
- -approx: Approximates to N decimal digits. {pos1 pos2 ... posN N}.
- -noise: Adds gausian noise. It requires as arguments mean and variance.
- -in: Database input path. Each attribute must be separated between tabs, with a first line of attribute names.
