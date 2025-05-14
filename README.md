# Match Three Solver

This project is in the match-three puzzle solver for game [Islands of Insight](https://store.steampowered.com/app/2071500/Islands_of_Insight/).

## Program Overview

### What does the program do?

The program solves match three puzzles. Program is mainly made for speed challenges in the Islands of Insight game - program takes a screenshot, input the puzzle into the GUI, solves it and inputs the solution into the game. 

## Running the Program

To run the program, you need to download the [.zip](match_three_solver.zip) file. Unzip it and run it by double clicking the runMe.bat file.

![program image](program.png)
 - The two sliders adjust width and height of the puzzle.
 - Grid in the middle is the puzzle to be solved by the problem. 
 - The first to "colors" on the bottom of GUI are black (for immovable blocks) and background/gray (for no color). You can add any color you want by pressing the plus sign and selecting the color. Once you select it, it is chosen and you can paint the grid with it and the color is also added to the bottom row of colors if you need it later.
 - Play button calculates solution for a given problem.
 - Reset button resets the program to the original state.
 - Camera button takes a screenshot of the problem, puts it in the grid and calculates the solution.
 - Auto solve feature triggers after the play or camera button if enabled. It inputs the correct moves after calculating the solution. It is many used for speed challenges in the game.
 - There is also an option to change the primary mouse button for the autosolve clicks.

## Authors

- [@hampter-o7](https://www.github.com/hampter-o7)


## License

[MIT](https://choosealicense.com/licenses/mit/)