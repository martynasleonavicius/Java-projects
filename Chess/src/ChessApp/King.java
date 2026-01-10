package ChessApp;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class King extends Piece{

    private boolean firstMove;
    private String[][] simpleBoard;
    private boolean isInCheck;
    private List<List<Integer>> specialMoves;
    private Set<List<Integer>> positionsOverWhichTheKingCanBeAttacked;

    public King(int x, int y, String fileName, Turn colour) throws Exception {
        super(x, y, fileName, colour);
        firstMove = true;
        //stores spaces occupied during castling
        specialMoves = new ArrayList<>();

        //Initialize a list which stores the directions trough which the king can be attacked. Use hashet to avoid duplication
        positionsOverWhichTheKingCanBeAttacked = new HashSet<>();

        //Initialise isInCheck flag
        isInCheck = false;

        //Initialize the simple board. This is used just for checking if the king is under attack or might need to be moved to safety
        simpleBoard = new String[8][8];
    }

    //We modify the move() superclass function so that castling function could be included
    //All we want to do is record if it is the first time the king moves
    @Override
    public void move(int newX, int newY){

        //If we want to castle our king, check if this is its first move
        if (firstMove){
            List<Integer> position = new ArrayList<>();
            position.add(newX);
            position.add(newY);

            //Check if the move is a castling move. This ensures that the right rook is moved in this maneuver
            if (specialMoves.contains(position)){
                //Now we need to find out if we're castling to the right
                if (newX < this.getX()){
                    //We thus move the rook leftward
                    Main.piecesOnBoard[2][this.getY()] = Main.piecesOnBoard[0][this.getY()];
                    Main.piecesOnBoard[0][this.getY()] = null;

                    //Of course, move the rook's image too
                    Main.piecesOnBoard[2][this.getY()].move(2, this.getY());
                }
                //Or to the left
                else{
                    //In this case we move the rook rightward
                    Main.piecesOnBoard[4][this.getY()] = Main.piecesOnBoard[7][this.getY()];
                    Main.piecesOnBoard[7][this.getY()] = null;

                    //Of course, move the rook's image too
                    Main.piecesOnBoard[4][this.getY()].move(4, this.getY());
                }
                specialMoves.clear();
            }
        }

        //Use this for regular king moves
        super.move(newX, newY);
        firstMove = false;
    }

    public List <List<Integer>> possibleMoves(){
        //Just copy the move finding function setup from the Queen class, but set the maximum number of spaces from the piece to check to 1
        int limit = 2;
        allPossibleMoves.clear();



        //We now try to look for available spaces to the left
        this.setDirectionToConsider(new int[]{-1, 0}, limit);
        //up
        this.setDirectionToConsider(new int[]{0, -1}, limit);
        //right
        this.setDirectionToConsider(new int[]{1, 0}, limit);
        //down
        this.setDirectionToConsider(new int[]{0, 1}, limit);
        //up left
        this.setDirectionToConsider(new int[]{-1, -1}, limit);
        //up right
        this.setDirectionToConsider(new int[]{1, -1}, limit);
        //down right
        this.setDirectionToConsider(new int[]{1, 1}, limit);
        //down left
        this.setDirectionToConsider(new int[]{-1, 1}, limit);

        //We first check if we can rook to the left
        castle(0);
        //Then the right
        castle(7);
        return allPossibleMoves;
    }

    private void castle(int x){
        //Do nothing if this is not the first king's move
        if (!firstMove){
            return;
        }

        System.out.println(allPossibleMoves.size());
        //We declare a new object. This is just a placeholder for we want to know if we actually have a rook to our left
        Object obj = new Object();

        // Check if the other one of the rooks are on the same rank
        if (Main.piecesOnBoard[x][this.getY()] == null) {
            return;
        }

        //Extract the object where the rook ought to be. If it is the tower class, check if the spaces between them are empty for castling to happen
        //To avoid any errors, we extract it as a superclass object. Thankfully, it turns into the original class we declared the object to be
        obj = (Piece) Main.piecesOnBoard[x][getY()];
        if (obj.getClass() != Tower.class || ((Piece) obj).getColour() != this.getColour() || !((Tower) obj).getIsFirstMove()){
            return;
        }

        // Check if there is a check preventing the king from castling


        //Checks if the squares between the king and the rook are empty. If not, do nothing
        int direction = x==7? -1 : 1;
        for (int i = direction; this.getX() - i < 7 && this.getX() - i > 0; i += direction){
            if (Main.piecesOnBoard[this.getX() - i][this.getY()] != null){
                return;
            }
        }

        //After all these tests for castling have past, add the special castling moves.
        // This will automatically check if the king is put in check or might traverse a check.
        List<List<Integer>> movesRight = this.getSpecialMovesForCastling(new int[]{-1, 0}, 3);
        if (movesRight.size() > 1){
            allPossibleMoves.addAll(movesRight);
            //specialMoves contains only the moves for castling. We need to save them, so that we would be able to switch the rook and the king
            specialMoves.add(movesRight.get(movesRight.size() - 1));
        }
        List<List<Integer>> movesLeft = this.getSpecialMovesForCastling(new int[]{1, 0}, 3);
        if (movesRight.size() > 1){
            allPossibleMoves.addAll(movesLeft);
            specialMoves.add(movesLeft.get(movesLeft.size() - 1));
        }

    }

    //This method checks all possible attack positions of the king.
    //We consider here the attacks from a bishop, rook, and a horse.
    public void kingAttackPositions(){
        positionsOverWhichTheKingCanBeAttacked.clear();
        List <int[]> directionsToConsider = new ArrayList<>();
        //Possible bishop and rook-like attacks
        directionsToConsider.add(new int[]{-1, 0});
        directionsToConsider.add(new int[]{-1, -1});
        directionsToConsider.add(new int[]{0, -1});
        directionsToConsider.add(new int[]{1, 0});
        directionsToConsider.add(new int[]{1, 1});
        directionsToConsider.add(new int[]{0, 1});
        directionsToConsider.add(new int[]{-1, 1});
        directionsToConsider.add(new int[]{1, -1});
        //Possible knight attacks on the king
        directionsToConsider.add(new int[]{-2, -1});
        directionsToConsider.add(new int[]{-1, -2});
        directionsToConsider.add(new int[]{1, -2});
        directionsToConsider.add(new int[]{2, -1});
        directionsToConsider.add(new int[]{2, 1});
        directionsToConsider.add(new int[]{1, 2});
        directionsToConsider.add(new int[]{-1, 2});
        directionsToConsider.add(new int[]{-2, 1});


        //Here we consider the bishop and tower moves
        for (int[] dir: directionsToConsider){
            int limit = 10;
            //Check if a knight's attack is being considered
            boolean knightAttack = dir[0] == 2 || dir[1] == 2 || dir[0] == -2 || dir[1] == -2;
            //If yes, change limit to 2. (Number of steps a knight can make is 1)
            if (knightAttack){
                limit = 2;
            }
            List <List <Integer>> singleDirection = getCheckDirectionToConsider(dir, limit);
            if (isTheSquareAtTheEndAnEnemyPiece(singleDirection, dir)){
                positionsOverWhichTheKingCanBeAttacked.addAll(singleDirection);
            }
        }

        //Raise isInCheck flag. It must be cured during the next moved.
        if (!positionsOverWhichTheKingCanBeAttacked.isEmpty()){
            isInCheck = true;
        }
    }


    //Method that checks if the king is under attack from a certain direction dir
    private boolean isTheSquareAtTheEndAnEnemyPiece(List<List<Integer>> enemyAttack, int[] dir){
        //First do a basic check. Let us see if the square at the end of the direction is occupied by an enemy piece
        char currentColour = this.getColour() == Turn.WHITE? 'W':'B';
        boolean basicCheck = !enemyAttack.isEmpty() &&
                !simpleBoard[enemyAttack.get(enemyAttack.size() - 1).get(0)][enemyAttack.get(enemyAttack.size() - 1).get(1)].equals("O") &&
                simpleBoard[enemyAttack.get(enemyAttack.size() - 1).get(0)][enemyAttack.get(enemyAttack.size() - 1).get(1)].charAt(1) != (currentColour);
        //No need to consider anything further if the basic check of an attack is failed.
        if (!basicCheck){
            return false;
        }

        //Get the piece at the end of the position
        String endPosition = simpleBoard[enemyAttack.get(enemyAttack.size() - 1).get(0)][enemyAttack.get(enemyAttack.size() - 1).get(1)];

        //Check if the king can be attack in a rook-like manner (i.e. attacked by a rook or a queen)
        boolean rookLikeAttack = dir[0] == 0 || dir[1] == 0 ;
        //Checks if the king can be attacked by a knight
        boolean knightAttack = dir[0] == 2 || dir[1] == 2 || dir[0] == -2 || dir[1] == -2;;

        //We check if the King could be attacked by hte enemy king in case a move is made
        boolean enemyKingAttack = false;
        if (enemyAttack.size() == 1){
            enemyKingAttack = endPosition.charAt(0) == 'K';
        }

        //Checks if there is a rook or a queen at the end of the direction
        if (rookLikeAttack){
            return endPosition.charAt(0) == 'Q' || endPosition.charAt(0) == 'R' || enemyKingAttack;
        }
        if (knightAttack){
            return endPosition.charAt(0) == 'Z';
        }

        //Finally check if the king can be attacked in a bishop-like manner
        else{
            return endPosition.charAt(0) == 'Q' || endPosition.charAt(0) == 'B' || enemyKingAttack;
        }
    }


    public void cureCheck(){
        positionsOverWhichTheKingCanBeAttacked.clear();
        isInCheck = false;
    }

    public void setSimpleBoard(String[][] simpleBoard){
        this.simpleBoard = simpleBoard;
    }

    public char returnClass(){
        return 'K';
    }




    //Here we add the method to see if the
    public List<List<Integer>> getCheckDirectionToConsider(int[] directionToConsider, int limit){
        List<List<Integer>> tempEnemyAttack = new ArrayList<>();
        setCheckDirectionToConsider(directionToConsider, limit, tempEnemyAttack);
        return tempEnemyAttack;
    }

    public void setCheckDirectionToConsider(int[] directionToConsider, int limit, List<List<Integer>> movesUnderConsideration){
        for (int i = 1; i < limit ;i++){
            if (addingPossibleAttackMoves(this.getX() + i * directionToConsider[0], this.getY() + i * directionToConsider[1], movesUnderConsideration)) break;
        }

    }

    public boolean addingPossibleAttackMoves(int x, int y, List<List<Integer>> movesUnderConsideration){
        //Let us first check if the inputs are still inbounds of the board. If not, we'll break the for-loop
        if (x >= 8 || x < 0 || y >= 8 || y < 0){
            return false;
        }


        boolean doWeNeedToStop = false;
        //We add all the empty places as possible spots to occupy
        if (simpleBoard[x][y].charAt(0) == 'O'){
            addPossibleAttackMovesToAReturnArray(x, y, movesUnderConsideration);
        }
        //Check for pieces. Remember that the piece can take opposite colour pieces, hence we allow a possible move on the nearest enemy piece
        else if (simpleBoard[x][y].charAt(1) != (this.getColour() == Turn.WHITE? 'W':'B')){
            addPossibleAttackMovesToAReturnArray(x, y, movesUnderConsideration);
            doWeNeedToStop = true;
        }
        //If the piece is the same colour, then we stop the process
        else {
            doWeNeedToStop = true;
        }
        return doWeNeedToStop;
    }

    public void addPossibleAttackMovesToAReturnArray(int x, int y, List<List<Integer>> movesUnderConsideration){
        List<Integer> position = new ArrayList<>();
        position.add(x);
        position.add(y);

        //Let us add the possible move it does not put our king into a check
        movesUnderConsideration.add(position);
    }


    public boolean checkIfInCheck(int fromX, int fromY, int x, int y){
        int oldKingX = -1;
        int oldKingY = -1;

        //Special consideration for the king. We must use its updated coordinates during the potential move
        if (fromX == this.getX() && fromY == this.getY()){
            oldKingY = this.getY();
            oldKingX = this.getX();
            this.setX(x);
            this.setY(y);
        }

        //If the potential move takes an enemy piece, let us save it to put it back in place when we are done checking
        String keepInMind = simpleBoard[x][y];
        //Make the possible move
        simpleBoard[x][y] = simpleBoard[fromX][fromY];
        simpleBoard[fromX][fromY] = "O";

        //List<List<Integer>> temp = new ArrayList<>();
        kingAttackPositions();
        boolean doesThisPutKingInCheck = !positionsOverWhichTheKingCanBeAttacked.isEmpty();
        //Check if the move puts YOUR king in check
        //if (this.getColour() == Turn.WHITE){

        //    doesThisPutKingInCheck = !positionsOverWhichTheKingCanBeAttacked.isEmpty();
            //temp.addAll(Main.whiteKing.getPositionsOverWhichTheKingCanBeAttacked());
        //    Main.whiteKing.cureCheck();
        //}
        //else{
        //    doesThisPutKingInCheck = Main.blackKing.getIsInCheck();
            //temp.addAll(Main.blackKing.getPositionsOverWhichTheKingCanBeAttacked());
        //    Main.blackKing.cureCheck();
        //}
        cureCheck();
        //reverse the move
        simpleBoard[fromX][fromY] = simpleBoard[x][y];
        //Now put enemy piece back in its place
        simpleBoard[x][y] = keepInMind;

        //Now, let us put the king back, if he was moved
        if (oldKingX > -1 && oldKingY > -1){
            this.setX(oldKingX);
            this.setY(oldKingY);
        }

        //System.out.println(doesThisPutKingInCheck);
        //System.out.println("Attack positions: " + temp);
        //Return the result
        return doesThisPutKingInCheck;
    }
}
