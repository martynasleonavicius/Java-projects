package ChessApp;


import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;

public abstract class Piece {

    //Store position of the king
    private int x;
    private int y;
    private ImageView pieceVisualisation;
    private Turn colour;
    //We make allPossibleMoves List public for easier access
    public List<List<Integer>> allPossibleMoves;

    public Piece(int x, int y, String fileName, Turn color) throws Exception{
        allPossibleMoves = new ArrayList<>();
        this.x = x;
        this.y = y;
        this.colour = color;
        this.pieceVisualisation = new ImageView(new Image(
                getClass().getResourceAsStream(fileName)
        ));
        this.pieceVisualisation.setFitWidth(50);
        this.pieceVisualisation.setFitHeight(50);
        move(x, y);
    }

    public abstract List <List<Integer>> possibleMoves();

    public ImageView getPiece(){
        return pieceVisualisation;
    }

    public Turn getColour(){
        return colour;
    }

    public int getX() { return x; }

    public int getY(){
        return y;
    }

    //This method sets the figure's visual in the new place.
    public void move(int newX, int newY){
        x = newX;
        y = newY;
        pieceVisualisation.setLayoutX((x * Main.WIDTH / 8) + 25);
        pieceVisualisation.setLayoutY((y * Main.WIDTH / 8) + 25);
    }


    //The following functions are useful for tower, bishop, king, and queen figures. That is why we put them here
    public boolean addingPossibleMovesAlongTheXOrYDirection(int x, int y, List<List<Integer>> movesUnderConsideration, boolean areWeConsideringActualMoves){
        //Let us first check if the inputs are still inbounds of the board. If not, we'll break the for-loop
        if (x >= 8 || x < 0 || y >= 8 || y < 0){
            return false;
        }


        boolean doWeNeedToStop = false;
        //We add all the empty places as possible spots to occupy
        if (Main.piecesOnBoard[x][y] == null){
            addPossibleMovesToAReturnArray(x, y, movesUnderConsideration, areWeConsideringActualMoves);
        }
        //Check for pieces. Remember that the piece can take opposite colour pieces, hence we allow a possible move on the nearest enemy piece
        else if (Main.piecesOnBoard[x][y].getColour() != this.getColour()){
            addPossibleMovesToAReturnArray(x, y, movesUnderConsideration, areWeConsideringActualMoves);
            doWeNeedToStop = true;
        }
        //If the piece is the same colour, then we stop the process
        else {
            doWeNeedToStop = true;
        }
        return doWeNeedToStop;
    }

    //Because the king checks possible positions over which it can be attacked, checkIfInCheck is getting called again.
    //That causes infinite recursion. That is why we need to use areWeConsideringActualMoves variable.
    //areWeConsideringActualMoves = true means we are generating possible moves of a regular piece. false is for finding if the king is in check
    //areWeConsideringActualMoves variable avoids infinite recursion.
    public void addPossibleMovesToAReturnArray(int x, int y, List<List<Integer>> movesUnderConsideration, boolean areWeConsideringActualMoves){
        List<Integer> position = new ArrayList<>();
        position.add(x);
        position.add(y);

        //Let us add the possible move it does not put our king into a check


        if(areWeConsideringActualMoves && !checkIfInCheck(x, y)){
            movesUnderConsideration.add(position);
        }

        //Add an "if" statement that add the attacking moves
        if (!areWeConsideringActualMoves){
            movesUnderConsideration.add(position);
        }
    }

    //For each possible move, this method checks if the king is in check after it.
    private boolean checkIfInCheck(int x, int y){
        int currentX = this.getX();
        int currentY = this.getY();

        //Let us make a simple copy of the current board. We will only use string representation of the pieces
        String[][] simpleBoard = new String[8][8];
        for (int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                if (Main.piecesOnBoard[i][j] == null){
                    simpleBoard[i][j] = "O";
                    //System.out.print(simpleBoard[i][j]);
                }
                else{
                    //Let us also encode color information
                    String colour = Main.piecesOnBoard[i][j].getColour() == Turn.WHITE? "W": "B";
                    simpleBoard[i][j] = Main.piecesOnBoard[i][j].returnClass() + colour;
                    //System.out.print(simpleBoard[i][j]);
                }
            }
        }

        //If the potential move takes an enemy piece, let us save it to put it back in place when we are done checking
        Piece keepInMind = Main.piecesOnBoard[x][y];
        //Make the possible move
        Main.piecesOnBoard[x][y] = Main.piecesOnBoard[currentX][currentY];
        Main.piecesOnBoard[x][y].move(x, y);
        Main.piecesOnBoard[currentX][currentY] = null;

        List<List<Integer>> temp = new ArrayList<>();

        boolean doesThisPutKingInCheck = false;
        //Check if the move puts YOUR king in check
        if (this.getColour() == Turn.WHITE){
            doesThisPutKingInCheck = Main.whiteKing.getIsInCheck();
            temp.addAll(Main.whiteKing.getPositionsOverWhichTheKingCanBeAttacked());
            Main.whiteKing.cureCheck();
        }
        else{
            doesThisPutKingInCheck = Main.blackKing.getIsInCheck();
            temp.addAll(Main.blackKing.getPositionsOverWhichTheKingCanBeAttacked());
            Main.blackKing.cureCheck();
        }
        //reverse the move
        Main.piecesOnBoard[currentX][currentY] = Main.piecesOnBoard[x][y];
        Main.piecesOnBoard[currentX][currentY].move(currentX, currentY);
        //Now put enemy piece back in its place
        Main.piecesOnBoard[x][y] = keepInMind;

        System.out.println(doesThisPutKingInCheck);
        System.out.println("Attack positions: " + temp);
        //Return the result
        return doesThisPutKingInCheck;
    }

    //For the majority of pieces, we will use the list allPossibleMoves to show where the selected piece can be placed.
    //But we want to utilise these existing functions to see if the king is in check.
    //We thus leverage method overloading.
    //This function receives a direction in which it has look for possible moves. Say [-1, 0] will consider possible moves to the left of the piece
    public void setDirectionToConsider(int[] directionToConsider, int limit, List<List<Integer>> movesUnderConsideration, boolean areWeConsideringActualMoves){
        for (int i = 1; i < limit ;i++){
            if (addingPossibleMovesAlongTheXOrYDirection(this.getX() + i * directionToConsider[0], this.getY() + i * directionToConsider[1], movesUnderConsideration, areWeConsideringActualMoves)) break;
        }

    }

    public List<List<Integer>> getDirectionToConsider(int[] directionToConsider, int limit){
        List<List<Integer>> tempEnemyAttack = new ArrayList<>();
        setDirectionToConsider(directionToConsider, limit, tempEnemyAttack,false);
        return tempEnemyAttack;
    }

    public void setDirectionToConsider(int[] directionToConsider, int limit){
        setDirectionToConsider(directionToConsider, limit, allPossibleMoves, true);
    }

    //we use method overloading for pieces which cannot go across the board fully, like the king.
    public void setDirectionToConsider(int[] directionToConsider){
        setDirectionToConsider(directionToConsider, 10, allPossibleMoves, true);
    }

    //method returning class
    public abstract char returnClass();
}
