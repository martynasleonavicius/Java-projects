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

    public void setX(int x){this.x = x;}

    public void setY(int y){this.y = y;}

    //This method sets the figure's visual in the new place.
    public void move(int newX, int newY){
        x = newX;
        y = newY;
        pieceVisualisation.setLayoutX((x * Main.WIDTH / 8) + 25);
        pieceVisualisation.setLayoutY((y * Main.WIDTH / 8) + 25);
    }


    //The following functions are useful for tower, bishop, king, and queen figures. That is why we put them here
    public boolean addingPossibleMovesAlongTheXOrYDirection(int x, int y, List<List<Integer>> movesUnderConsideration, boolean isThisPawn, boolean pawnAttackConsideration){
        //Let us first check if the inputs are still inbounds of the board. If not, we'll break the for-loop
        if (x >= 8 || x < 0 || y >= 8 || y < 0){
            return false;
        }

        //The following few lines address a special case when a pawn captures an enemy pawn via en passant
        if(isThisPawn && pawnAttackConsideration){
            pawnTakesEnPassant(x, y, movesUnderConsideration);
        }

        boolean doWeNeedToStop = false;
        //We add all the empty places as possible spots to occupy
        //Make sure that the pawns cannot move sideways. By the rules we have already implemented, the pawn can only move to the side if and only if it is attaking an enemy piece
        if (Main.piecesOnBoard[x][y] == null){
            if (!pawnAttackConsideration && isThisPawn){
                addPossibleMovesToAReturnArray(x, y, movesUnderConsideration);
            }
            if (!isThisPawn){
                addPossibleMovesToAReturnArray(x, y, movesUnderConsideration);
            }
        }

        //Check for pieces. Remember that the piece can take opposite colour pieces, hence we allow a possible move on the nearest enemy piece
        else if (Main.piecesOnBoard[x][y].getColour() != this.getColour()){
            if (!isThisPawn){
                addPossibleMovesToAReturnArray(x, y, movesUnderConsideration);
                doWeNeedToStop = true;
            }
            if (isThisPawn && pawnAttackConsideration){
                addPossibleMovesToAReturnArray(x, y, movesUnderConsideration);
                doWeNeedToStop = true;
            }
        }



        //If the piece is the same colour, then we stop the process
        else {
            doWeNeedToStop = true;
        }
        return doWeNeedToStop;
    }

    public void pawnTakesEnPassant(int x, int y,  List<List<Integer>> movesUnderConsideration){
        List<Integer> position = new ArrayList<>();
        position.add(x);
        position.add(y);
        //System.out.println(Main.enPassantSquares);
        //System.out.println(Main.enPassantSquares.contains(position));

        if (Main.enPassantSquares.contains(position) && !checkIfKingInCheck(x, y)){
            movesUnderConsideration.add(position);
            //System.out.println("pTEP: " + movesUnderConsideration);
        }
    }

    //Because the king checks possible positions over which it can be attacked, checkIfInCheck is getting called again.
    //As we need special consideration for the pawns, we will use a boolean variable to record which piece we are moving!
    public void addPossibleMovesToAReturnArray(int x, int y, List<List<Integer>> movesUnderConsideration){
        List<Integer> position = new ArrayList<>();
        position.add(x);
        position.add(y);

        //Let us add the possible move it does not put our king into a check
        if(!checkIfKingInCheck(x, y)){
            movesUnderConsideration.add(position);
        }
    }

    //For each possible move, this method checks if the king is in check after it.
    private boolean checkIfKingInCheck(int x, int y){
        //int currentX = this.x;
        //int currentY = this.y;

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

        if (colour == Turn.WHITE){
            Main.whiteKing.setSimpleBoard(simpleBoard);
            return Main.whiteKing.checkIfInCheck(this.x, this.y, x, y);
        }
        else {
            Main.blackKing.setSimpleBoard(simpleBoard);
            return Main.blackKing.checkIfInCheck(this.x, this.y, x, y);
        }

    }


    //For the majority of pieces, we will use the list allPossibleMoves to show where the selected piece can be placed.
    //We thus use method overloading.
    //This function receives a direction in which it has look for possible moves. Say [-1, 0] will consider possible moves to the left of the piece
    public void setDirectionToConsider(int[] directionToConsider, int limit, List<List<Integer>> movesUnderConsideration, boolean isThisPawn, boolean pawnAttackConsideration){
        for (int i = 1; i < limit ;i++){
            if (addingPossibleMovesAlongTheXOrYDirection(this.getX() + i * directionToConsider[0], this.getY() + i * directionToConsider[1], movesUnderConsideration, isThisPawn, pawnAttackConsideration)) break;
        }

    }

    //Used for castling.
    public List<List<Integer>> getSpecialMovesForCastling(int[] directionToConsider, int limit){
        List<List<Integer>> temp = new ArrayList<>();
        setDirectionToConsider(directionToConsider, limit, temp, false, false);
        return temp;
    }

    //We introduce isThisPawn and pawnAttackConsideration flags, as they require different moves to other chess pieces
    public void pawnDirectionConsiderations(int[] directionToConsider, int limit,  boolean pawnAttackConsideration){
        setDirectionToConsider(directionToConsider, limit, allPossibleMoves, true, pawnAttackConsideration);
    }

    public void setDirectionToConsider(int[] directionToConsider, int limit){
        setDirectionToConsider(directionToConsider, limit, allPossibleMoves, false, false);
    }

    //we use method overloading for pieces which cannot go across the board fully, like the king.
    public void setDirectionToConsider(int[] directionToConsider){
        setDirectionToConsider(directionToConsider, 10, allPossibleMoves, false, false);
    }

    //method returning class
    public abstract char returnClass();
}
