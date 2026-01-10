package ChessApp;

import java.util.List;
import java.util.ArrayList;

public class Pawn extends Piece{

    private boolean isFirstMove;

    public Pawn (int x, int y, String fileName, Turn colour) throws Exception{
        super(x, y, fileName, colour);
        isFirstMove = true;
    }

    @Override
    public List<List<Integer>> possibleMoves() {
        allPossibleMoves.clear();
        //This variable determines which direction we consider for moving. 1 will be for white pawns, -1 for black. Remember, Y direction goes from top down on screen
        int direction = 1;
        if (this.getColour() == Turn.BLACK){
            direction *= -1;
        }

        //When the pawn moves for the first time, it may move 2 spaces forward
        int limit = 2;
        if (isFirstMove){
            limit = 3;
        }

        //This is for considering forward, non-attacking positions. false flag is making sure we are only considering non-attacking positions
        this.pawnDirectionConsiderations(new int[]{0, direction}, limit, false);
        //Now let us consider the attacking positions...
        //to the left
        this.pawnDirectionConsiderations(new int[]{1, direction}, 2, true);
        //...and to the right
        this.pawnDirectionConsiderations(new int[]{-1, direction}, 2, true);
        return allPossibleMoves;
    }

    //WE must override superclass move method, so that we could change isFirstMove flag to false after the first move was made
    @Override
    public void move(int newX, int newY){
        if (isFirstMove) enPassant(newY);
        super.move(newX, newY);
        isFirstMove = false;
        arrivedAtTheEnd(newY);
        //System.out.println(this.getX() + " " + this.getY());
    }

    //Create a method which tracks if the pawn moves enough squares to be vulnerable for en passant
    private void enPassant(int newY){
        int delta = this.getY() - newY;
        List<Integer> position = new ArrayList<>();
        position.add(getX());
        //Only white pawns could have a +ve delta
        //System.out.println(delta);
        if (delta > 0){
            position.add(newY+1);
            Main.enPassantSquares.add(position);
        }
        if (delta < 0){
            position.add(newY-1);
            Main.enPassantSquares.add(position);
        }
        //System.out.println(Main.enPassantSquares);
    }

    //If the pawn has reached the other side of the board, we must trigger new piece selection piece method
    private void arrivedAtTheEnd(int newY){
        if (newY == 7 || newY == 0){
            Main.choseNewPiece(this.getX(), this.getY());
        }
    }

    public char returnClass(){
        return 'P';
    }
}
