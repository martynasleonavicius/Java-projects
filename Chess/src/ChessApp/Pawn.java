package ChessApp;

import java.util.List;
import java.util.ArrayList;

public class Pawn extends Piece{

    private boolean isFirstMove;

    public Pawn (int x, int y, String fileName, Turn colour) throws Exception{
        super(x, y, fileName, colour);
        isFirstMove = true;
    }

    //We need to calculate the possible moves differently if it is the first movement of the piece
    //Not meant to be accessed outside this class
    private List<List<Integer>> firstMove(){
        List <List<Integer>> possibleMoves = new ArrayList<>();
        List <Integer> positions = new ArrayList<>();
        int x = super.getX();
        int y = super.getY();

        //Calculates possible positions.
        for (int i = 0; i < 2 && i + y < Main.WIDTH/8; i++){
            positions = new ArrayList<>();

            //If we have a white piece, it needs to go down (y increases)
            if (this.getColour() == Turn.WHITE){
                y++;
            }
            //Black piece go up the board (y decreases)
            else{
                y--;
            }

            //We omit any moves that might require us to move into an occupied square
            if (Main.piecesOnBoard[x][y] != null){
                break;
            }

            //Otherwise we can easily consider them
            positions.add(x);
            positions.add(y);

            possibleMoves.add(positions);
        }

        return possibleMoves;
    }

    //Regular moves involve moving the pawn 1 square up.
    //Not meant to be accessed outside this class
    private List<List<Integer>> regularMoves(){
        List<List<Integer>> possibleMoves = new ArrayList<>();
        List <Integer> positions = new ArrayList<>();
        positions.add(super.getX());

        //White move down the window (y increases)
        if (this.getColour() == Turn.WHITE){
            positions.add(super.getY()+1);
        }
        //Black moves up the window (y decreases)
        else{
            positions.add(super.getY()-1);
        }

        //Here we only add possible move if the square in front is free
        if (Main.piecesOnBoard[positions.get(0)][positions.get(1)] == null){
            possibleMoves.add(positions);
        }

        return possibleMoves;
    }

    //Cheks if there are any pieces that can be taken to the left of the pawn
    //keep this public in case I want to paint the attackable pieces a different color
    public List <List<Integer>> piecesToBeAttackedOnLeft() throws Exception{
        List<List<Integer>> squaresThatCanBeAttacked = new ArrayList<>();
        List<Integer> positions = new ArrayList<>();

        //The following possible attack squares are only valid for black pawns
        if (this.getColour() == Turn.BLACK){
            //Check if anything exists to the top right of the black piece and if it has the opposite color to it
            if (Main.piecesOnBoard[super.getX() - 1][super.getY() - 1] != null && Main.piecesOnBoard[super.getX() - 1][super.getY() - 1].getColour() != this.getColour()) {
                positions.add(super.getX() - 1);
                positions.add(super.getY() - 1);
                squaresThatCanBeAttacked.add(positions);
            }
        }
        //And now for white pieces
        else{

            if (Main.piecesOnBoard[super.getX() + 1][super.getY() +1] != null && Main.piecesOnBoard[super.getX() + 1][super.getY() + 1].getColour() != this.getColour()) {
                positions.add(super.getX() + 1);
                positions.add(super.getY() + 1);
                squaresThatCanBeAttacked.add(positions);
            }
        }

        return squaresThatCanBeAttacked;
    }

    //Now check the same thing to the right
    public List <List<Integer>> piecesToBeAttackedOnRight() throws Exception{
        List<List<Integer>> squaresThatCanBeAttacked = new ArrayList<>();
        List<Integer> positions = new ArrayList<>();

        if (this.getColour() == Turn.BLACK){
            //Check if anything exists to the top right of the black piece and if it has the opposite color to it
            if (Main.piecesOnBoard[super.getX() + 1][super.getY() - 1] != null && Main.piecesOnBoard[super.getX() + 1][super.getY() - 1].getColour() != this.getColour()) {
                positions.add(super.getX() + 1);
                positions.add(super.getY() - 1);
                squaresThatCanBeAttacked.add(positions);
            }
        }
        //And now  check for white pieces
        else{
            if (Main.piecesOnBoard[super.getX() - 1][super.getY() + 1] != null && Main.piecesOnBoard[super.getX() - 1][super.getY() + 1].getColour() != this.getColour()){
                positions.add(super.getX() - 1);
                positions.add(super.getY() + 1);
                squaresThatCanBeAttacked.add(positions);
            }
        }
        return squaresThatCanBeAttacked;
    }


    @Override
    public List<List<Integer>> possibleMoves() {
        //If it is the 1st time the pawn is moving, we need to show the ability to move 2 tiles up
        List<List<Integer>> allPossibleMoves = new ArrayList<>();

        if ((super.getY() == 1 && this.getColour() == Turn.WHITE)|| (super.getY() == 6) && this.getColour() == Turn.BLACK) {
            allPossibleMoves = firstMove();
        }
        //If it is not, then we just regular pawn movements
        else {
            allPossibleMoves = regularMoves();
        }

        //Now add all the pieces that can be attacked. ArrayIndexOutOfBoundsError is thrown if the place that is being checked is outside the board. We shall ignore them
        try {
            allPossibleMoves.addAll(piecesToBeAttackedOnLeft());
        } catch(Exception e){}
        try{
            allPossibleMoves.addAll(piecesToBeAttackedOnRight());
        } catch(Exception e) {}


        return allPossibleMoves;
    }

    public char returnClass(){
        return 'P';
    }
}
