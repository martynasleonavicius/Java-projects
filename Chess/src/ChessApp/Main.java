package ChessApp;

import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.scene.shape.Circle;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Dialog;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main extends Application{

    //Declare the size of the board
    public static int WIDTH = 800;
    public static Piece[][] piecesOnBoard;
    //Monitors whose turn is it
    public static Turn turn = Turn.WHITE;


    //Let us document both kings' positions, so that we could easily see if any of them are in check, when it is the respective colour's turn
    public static King whiteKing;
    public static King blackKing;
    public static Stage stage;
    public static Pane clickHandle =  new Pane();

    //This list tracks all en passant viable squares
    public static List<List<Integer>> enPassantSquares = new ArrayList<>();

    @Override
    public void start(Stage stage) throws Exception{
        //For painting the chess board
        Canvas chessBoard = new Canvas(WIDTH, WIDTH);
        GraphicsContext painter = chessBoard.getGraphicsContext2D();

        //Handles piece image placement and clicks
        StackPane wrapPaneAndCanvas = new StackPane();
        wrapPaneAndCanvas.getChildren().addAll(chessBoard, clickHandle);

        //Another pane for piece promotion
        Pane promotionWindow = new StackPane();
        promotionWindow.setVisible(false);      //Let this be invisible at first
        promotionWindow.setManaged(false);      //doesn't affect the layout when hidden
        promotionWindow.setPickOnBounds(true);  //Blocks picks when the window is open

        //Set the square that will be drawn to black colour
        painter.setFill(Color.BLUE);
        boardCreation(painter);

        //Add a piece. Place pieces in a list.
        //Piece[][] piecesOnBoard = new Piece[8][8];
        piecesOnBoard = new Piece[8][8];
        piecePlacement(clickHandle, piecesOnBoard);

        //When the suggested moves are generated, we need to remember them in an array for removal when another piece is selected.
        List<Circle> circles = new ArrayList<>();
        //Save currently selected square [x coordinate, y coordinate]
        List<Integer> selectedSquare = new ArrayList<>();

        List<Integer> selectedPieceCoordinates = new ArrayList<>();


        clickHandle.setOnMouseClicked(event ->{
            int x = (int) event.getX()*8/WIDTH;
            int y = (int) event.getY()*8/WIDTH;
            System.out.println("X: " + x + " Y: " + y);
            //Save the selected square coordinates as some sort of action might be performed on it
            selectedSquare.add(x);
            selectedSquare.add(y);

            if (!selectedPieceCoordinates.isEmpty() && piecesOnBoard[selectedPieceCoordinates.get(0)][selectedPieceCoordinates.get(1)].possibleMoves().contains(selectedSquare)){
                movingPieces(selectedSquare, selectedPieceCoordinates);
                //whiteKing.cureCheck();
                //blackKing.cureCheck();
            }

            //Removes the old possible moves
            possiblePlacementRemoval(clickHandle, circles);
            //Draws possible moves of a newly selected piece
            if (piecesOnBoard[x][y] != null && piecesOnBoard[x][y].getColour() == turn){
                selectPieceToMoveItLater(selectedPieceCoordinates, circles, x, y);

            }

            //Deselect the moved piece
            if (piecesOnBoard[x][y] == null){
                selectedPieceCoordinates.clear();
            }
            //System.out.println("selectedPieceCoordinates: " + selectedPieceCoordinates);
            //System.out.println("selectedSquare: " + selectedSquare);
            selectedSquare.clear();
            printBoard();
        });

        Scene scene = new Scene(wrapPaneAndCanvas);
        stage.setScene(scene);
        stage.setTitle("Chess");
        stage.show();
    }

    //A method to save selection of a piece to be moved later on
    public void selectPieceToMoveItLater(List <Integer> selectedPieceCoordinates, List <Circle> circles, int row, int column){
        //Draws possible moves of a selected piece
        possiblePlacementView(clickHandle, piecesOnBoard[row][column].possibleMoves(), circles);

        //isPieceSelected = true;
        selectedPieceCoordinates.clear();
        selectedPieceCoordinates.add(row);
        selectedPieceCoordinates.add(column);
    }

    //Method responsible for actually moving pieces on the board
    public void movingPieces(List<Integer> selectedSquare, List<Integer> selectedPieceCoordinates){

        int startingSize = enPassantSquares.size();

        //Delete the picture of a captured piece.
        //We first check if there is a picture to delete and then if the piece is of a different colour
        if (piecesOnBoard[selectedSquare.get(0)][selectedSquare.get(1)] != null && piecesOnBoard[selectedSquare.get(0)][selectedSquare.get(1)].getColour() != turn){
            clickHandle.getChildren().remove(piecesOnBoard[selectedSquare.get(0)][selectedSquare.get(1)].getPiece());
        }

        //Check if the new square is en passant capture
        if (    piecesOnBoard[selectedPieceCoordinates.get(0)][selectedPieceCoordinates.get(1)].returnClass() == 'P' &&
                enPassantSquares.contains(selectedSquare)) enPassantCapture();

        //Copy figure information to a new square. This line also deletes any information about the piece which existed there before
        piecesOnBoard[selectedSquare.get(0)][selectedSquare.get(1)] = piecesOnBoard[selectedPieceCoordinates.get(0)][selectedPieceCoordinates.get(1)];

        //Redraw the figure in the new place
        piecesOnBoard[selectedSquare.get(0)][selectedSquare.get(1)].move(selectedSquare.get(0), selectedSquare.get(1));

        //Delete the picture of the piece from the previously occupied space
        clickHandle.getChildren().remove(piecesOnBoard[selectedPieceCoordinates.get(0)][selectedPieceCoordinates.get(1)].getPiece());

        //Add the picture to the new space. Make sure that we haven't added a new piece via promotion
        if (!clickHandle.getChildren().contains(piecesOnBoard[selectedSquare.get(0)][selectedSquare.get(1)].getPiece())){
            clickHandle.getChildren().add(piecesOnBoard[selectedSquare.get(0)][selectedSquare.get(1)].getPiece());
        }

        //Set the old occupied space as null
        piecesOnBoard[selectedPieceCoordinates.get(0)][selectedPieceCoordinates.get(1)] = null;

        //Deselect the moved piece
        selectedPieceCoordinates.clear();

        //After making a move, it is the opponent's turn
        changeTurn();

        int endingSize = enPassantSquares.size();

        //We compare the sizes of the list containing en passant sizes. We remove the first one added.
        if (startingSize >= 1 && endingSize >= 1){
            enPassantSquares.remove(0);
        }
    }

    //Function that handles en passant
    public void enPassantCapture(){
        //We need displacement adjust the Y coordinate to the current en passant square, so that we could capture the relevant piece
        int displacement = -1;
        //Delete the image of the captured piece and a reference to it on the board
        if (turn == Turn.BLACK) displacement = 1;
        clickHandle.getChildren().remove(piecesOnBoard[enPassantSquares.get(0).get(0)][enPassantSquares.get(0).get(1) + displacement].getPiece());
        piecesOnBoard[enPassantSquares.get(0).get(0)][enPassantSquares.get(0).get(1) + displacement] = null;
    }


    //When a different piece is selected, remove all possible moves previously drawn
    public void possiblePlacementRemoval(Pane clickHandle, List<Circle> circles){
        circles.forEach(circle -> clickHandle.getChildren().remove(circle));
        circles.clear();
    }

    //a method that shows possible moves
    public void possiblePlacementView(Pane clickHandle, List<List<Integer>> listOfPositions, List<Circle> circles){
        listOfPositions.forEach(availablePosition -> {
            Circle circle = new Circle(availablePosition.get(0)*WIDTH/8 + WIDTH/16, availablePosition.get(1)*WIDTH/8 + WIDTH/16, 25, Color.GREEN);
            clickHandle.getChildren().add(circle);
            circles.add(circle);
        });
    }


    public void piecePlacement1(Pane clickHandle, Piece[][] piecesOnBoard) throws Exception{
        //Queen q = new Queen(0, 0, "/w_queen.png", Turn.WHITE);
        //clickHandle.getChildren().add(q.getPiece());
        //piecesOnBoard[0][0] = q;

        //Bishop b_bishop = new Bishop(2, 7, "/b_bishop.png", Turn.BLACK);
        //clickHandle.getChildren().add(b_bishop.getPiece());
        //piecesOnBoard[b_bishop.getX()][b_bishop.getY()] = b_bishop;

        King bking = new King(3, 7, "/b_king.png", Turn.BLACK);
        clickHandle.getChildren().add(bking.getPiece());
        piecesOnBoard[bking.getX()][bking.getY()] = bking;
        blackKing = bking;

        //Tower tower = new Tower(7, 7, "/b_tower.png", Turn.BLACK);
        //clickHandle.getChildren().add(tower.getPiece());
        //piecesOnBoard[tower.getX()][tower.getY()] = tower;

        //Tower tower1 = new Tower(0, 7, "/b_tower.png", Turn.BLACK);
        //clickHandle.getChildren().add(tower1.getPiece());
        //piecesOnBoard[tower1.getX()][tower1.getY()] = tower1;

        King king = new King(3, 4, "/w_king.png", Turn.WHITE);
        clickHandle.getChildren().add(king.getPiece());
        piecesOnBoard[king.getX()][king.getY()] = king;
        whiteKing = king;

        //Knight k = new Knight(1, 2, "/b_knight.png", Turn.BLACK);
        //clickHandle.getChildren().add(k.getPiece());
        //piecesOnBoard[k.getX()][k.getY()] = k;

        Pawn wp = new Pawn(6, 1, "/w_pawn.png", Turn.WHITE);
        clickHandle.getChildren().add(wp.getPiece());
        piecesOnBoard[wp.getX()][wp.getY()] = wp;


        Pawn bp = new Pawn(7, 6, "/b_pawn.png", Turn.BLACK);
        clickHandle.getChildren().add(bp.getPiece());
        piecesOnBoard[bp.getX()][bp.getY()] = bp;
    }


    //Places initial pieces
    public void piecePlacement(Pane clickHandle, Piece[][] piecesOnBoard) throws Exception{

        //Add Kings
        King w_king = new King(3, 0, "/w_king.png", Turn.WHITE);
        clickHandle.getChildren().add(w_king.getPiece());
        piecesOnBoard[3][0] = w_king;
        whiteKing = w_king;

        King b_king = new King(3, 7, "/b_king.png", Turn.BLACK);
        clickHandle.getChildren().add(b_king.getPiece());
        piecesOnBoard[3][7] = b_king;
        blackKing = b_king;

        //Add Queens
        Queen w_queen = new Queen(4, 0, "/w_queen.png", Turn.WHITE);
        //painter.drawImage(w_queen.getPiece(), 425, 25, 50, 50);
        clickHandle.getChildren().add(w_queen.getPiece());
        piecesOnBoard[4][0] = w_queen;

        Queen b_queen = new Queen(4, 7, "/b_queen.png", Turn.BLACK);
        //painter.drawImage(b_queen.getPiece(), 425, 725, 50, 50);
        clickHandle.getChildren().add(b_queen.getPiece());
        piecesOnBoard[4][7] = b_queen;


        //Add Knights
        Knight w_knight1 = new Knight(1, 0, "/w_knight.png", Turn.WHITE);
        //painter.drawImage(w_knight1.getPiece(), 225, 25, 50, 50);
        clickHandle.getChildren().add(w_knight1.getPiece());
        piecesOnBoard[1][0] = w_knight1;
        Knight w_knight2 = new Knight(6, 0, "/w_knight.png", Turn.WHITE);
        //painter.drawImage(w_knight2.getPiece(), 525, 25, 50, 50);
        clickHandle.getChildren().add(w_knight2.getPiece());
        piecesOnBoard[6][0] = w_knight2;

        Knight b_knight1 = new Knight(1, 7, "/b_knight.png", Turn.BLACK);
        //painter.drawImage(b_knight1.getPiece(), 225, 725, 50, 50);
        clickHandle.getChildren().add(b_knight1.getPiece());
        piecesOnBoard[1][7] = b_knight1;
        Knight b_knight2 = new Knight(6, 7, "/b_knight.png", Turn.BLACK);
        //painter.drawImage(b_knight2.getPiece(), 525, 725, 50, 50);
        clickHandle.getChildren().add(b_knight2.getPiece());
        piecesOnBoard[6][7] = b_knight2;

        //Add bishops
        Bishop w_bishop1 = new Bishop(2, 0, "/w_bishop.png", Turn.WHITE);
        //painter.drawImage(w_bishop1.getPiece(), 125, 25, 50, 50);
        clickHandle.getChildren().add(w_bishop1.getPiece());
        piecesOnBoard[2][0] = (w_bishop1);
        Bishop w_bishop2 = new Bishop(5, 0, "/w_bishop.png", Turn.WHITE);
        //painter.drawImage(w_bishop2.getPiece(), 625, 25, 50, 50);
        clickHandle.getChildren().add(w_bishop2.getPiece());
        piecesOnBoard[5][0] = (w_bishop2);

        Bishop b_bishop1 = new Bishop(2, 7, "/b_bishop.png", Turn.BLACK);
        //painter.drawImage(b_bishop1.getPiece(), 125, 725, 50, 50);
        clickHandle.getChildren().add(b_bishop1.getPiece());
        piecesOnBoard[2][7] = (b_bishop1);
        Bishop b_bishop2 = new Bishop(5, 7, "/b_bishop.png", Turn.BLACK);
        //painter.drawImage(b_bishop2.getPiece(), 625, 725, 50, 50);
        clickHandle.getChildren().add(b_bishop2.getPiece());
        piecesOnBoard[5][7] = (b_bishop2);

        //Add Towers
        Tower w_tower1 = new Tower(0, 0, "/w_tower.png", Turn.WHITE);
        //painter.drawImage(w_tower1.getPiece(), 25, 25, 50, 50);
        clickHandle.getChildren().add(w_tower1.getPiece());
        piecesOnBoard[0][0] = (w_tower1);
        Tower w_tower2 = new Tower(7, 0, "/w_tower.png", Turn.WHITE);
        //painter.drawImage(w_tower2.getPiece(), 725, 25, 50, 50);
        clickHandle.getChildren().add(w_tower2.getPiece());
        piecesOnBoard[7][0] = (w_tower2);

        Tower b_tower1 = new Tower(0, 7, "/b_tower.png", Turn.BLACK);
        //painter.drawImage(b_tower1.getPiece(), 25, 725, 50, 50);
        clickHandle.getChildren().add(b_tower1.getPiece());
        piecesOnBoard[0][7] = (b_tower1);
        Tower b_tower2 = new Tower(7, 7, "/b_tower.png", Turn.BLACK);
        //painter.drawImage(b_tower2.getPiece(), 725, 725, 50, 50);
        clickHandle.getChildren().add(b_tower2.getPiece());
        piecesOnBoard[7][7] = (b_tower2);

        //Add pawns
        Pawn pawn = new Pawn(0, 1, "/w_pawn.png", Turn.WHITE);
        for (int i = 1; i < 7; i += 5){
            for (int j = 0; j < 8; j++){
                //When i = 1, we place white pieces
                if (i == 1) {
                    pawn = new Pawn(j, i, "/w_pawn.png", Turn.WHITE);
                }
                //Now we'll place the black ones
                else{
                    pawn = new Pawn(j, i, "/b_pawn.png", Turn.BLACK);
                }
                //painter.drawImage(pawn.getPiece(), 25 + j*100, 25 + i*100, 50, 50);
                clickHandle.getChildren().add(pawn.getPiece());
                piecesOnBoard[j][i] = (pawn);
            }
        }
    }


    //Draws chessboard
    public void boardCreation(GraphicsContext painter){

        for (int x = 0; x < WIDTH/100; x++){
            for (int y = 0; y < WIDTH/100; y += 2){
                if (x%2 == 1) {
                    painter.fillRect(x*100, y*100, WIDTH/8, WIDTH/8);
                }
                //Introduces an offset to actually draw the board.
                else{
                    painter.fillRect(x*100, y*100 + 100, WIDTH/8, WIDTH/8);
                }
            }
        }
    }


    public void printBoard(){
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                if (piecesOnBoard[j][i] == null){
                    System.out.print("[-]");
                }
                else{
                    System.out.print("[" + piecesOnBoard[j][i].returnClass() + "]");
                }
            }
            System.out.println();
        }
    }

    public static void choseNewPiece(int x, int y){
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Promote pawn");
        dialog.setHeaderText("Choose a new piece");

        //Make the dialog screen modal (block interaction until it is resolved)
        dialog.initOwner(stage);
        dialog.initModality(Modality.APPLICATION_MODAL);


        //Create buttons which allow the player to choose a new piece
        ButtonType queenBtn  = new ButtonType("Queen",  ButtonBar.ButtonData.OK_DONE);
        ButtonType rookBtn   = new ButtonType("Rook",   ButtonBar.ButtonData.OK_DONE);
        ButtonType bishopBtn = new ButtonType("Bishop", ButtonBar.ButtonData.OK_DONE);
        ButtonType knightBtn = new ButtonType("Knight", ButtonBar.ButtonData.OK_DONE);

        //Now add buttons to the dialog screen
        dialog.getDialogPane().getButtonTypes().addAll(
                queenBtn, rookBtn, bishopBtn, knightBtn
        );

        //Attach the behaviour to actual buttons
        Button queen = (Button) dialog.getDialogPane().lookupButton(queenBtn);
        Button rook = (Button) dialog.getDialogPane().lookupButton(rookBtn);
        Button bishop = (Button) dialog.getDialogPane().lookupButton(bishopBtn);
        Button knight = (Button) dialog.getDialogPane().lookupButton(knightBtn);

        //Prevent the dialog box from closing without a promotion
        final boolean[] allowClose = {false};
        dialog.setOnCloseRequest(e -> {
            if (!allowClose[0]) e.consume();
        });

        //Now we check which button is pressed, and then promote it to that one
        queen.setOnAction(e -> {
            queenPromotion(x, y);
            allowClose[0] = true;
            dialog.close();
        });

        rook.setOnAction(e -> {
            rookPromotion(x, y);
            allowClose[0] = true;
            dialog.close();
        });

        bishop.setOnAction(e -> {
            bishopPromotion(x, y);
            allowClose[0] = true;
            dialog.close();
        });

        knight.setOnAction(e -> {
            knightPromotion(x, y);
            allowClose[0] = true;
            dialog.close();
        });

        //Shows the dialog button. Something must be chosen so that the game would continue
        dialog.showAndWait();
    }

    public static void queenPromotion(int x, int y){
        String typeOfPiece= turn == Turn.WHITE? "/w_queen.png" : "/b_queen.png";

        //Remove the old pawn
        pieceRemoval(x, y);
        try {
            //add a new piece depending on whose turn it is
            Tower newPiece = new Tower(x, y, typeOfPiece, turn);
            pieceAdder(x, y, newPiece);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void rookPromotion(int x, int y){
        String typeOfPiece= turn == Turn.WHITE? "/w_tower.png" : "/b_tower.png";

        pieceRemoval(x, y);
        try {
            //add a new piece depending on whose turn it is
            Tower newPiece = new Tower(x, y, typeOfPiece, turn);
            pieceAdder(x, y, newPiece);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void bishopPromotion(int x, int y){
        String typeOfPiece= turn == Turn.WHITE? "/w_bishop.png" : "/b_bishop.png";
        pieceRemoval(x, y);
        try {
            //add a new piece depending on whose turn it is
            Bishop newPiece = new Bishop(x, y, typeOfPiece, turn);
            pieceAdder(x, y, newPiece);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void knightPromotion(int x, int y){
        String typeOfPiece= turn == Turn.WHITE? "/w_knight.png" : "/b_knight.png";
        pieceRemoval(x, y);
        try {
            //add a new piece depending on whose turn it is
            Knight newPiece = new Knight(x, y, typeOfPiece, turn);
            pieceAdder(x, y, newPiece);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Method to remove piece of the board
    public static void pieceRemoval(int x, int y){
        clickHandle.getChildren().remove(piecesOnBoard[x][y].getPiece());
        piecesOnBoard[x][y] = null;
    }

    //Method to add pieces
    public static void pieceAdder(int x, int y, Piece piece){
        piecesOnBoard[x][y] = piece;
        clickHandle.getChildren().add(piece.getPiece());
    }

    public static void changeTurn(){
        turn = (turn == Turn.WHITE) ? Turn.BLACK: Turn.WHITE;
    }

    public static void main(String[] args) {
        launch(Main.class);
    }
}
