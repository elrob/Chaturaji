/*globals game_info, Kinetic, myName, TEST_MODE, WebSocket, webSocketAddress, alert */

var image_sources = {
    p1Pawn: './images/Red_Pawn.svg',
    p1King: './images/Red_King.svg',
    p1Boat: './images/Red_Boat.svg',
    p1Elephant: './images/Red_Elephant.svg',
    p1Knight: './images/Red_Knight.svg',
    p2Pawn: './images/Green_Pawn.svg',
    p2King: './images/Green_King.svg',
    p2Boat: './images/Green_Boat.svg',
    p2Elephant: './images/Green_Elephant.svg',
    p2Knight: './images/Green_Knight.svg',
    p3Pawn: './images/Blue_Pawn.svg',
    p3King: './images/Blue_King.svg',
    p3Boat: './images/Blue_Boat.svg',
    p3Elephant: './images/Blue_Elephant.svg',
    p3Knight: './images/Blue_Knight.svg',
    p4Pawn: './images/Yellow_Pawn.svg',
    p4King: './images/Yellow_King.svg',
    p4Boat: './images/Yellow_Boat.svg',
    p4Elephant: './images/Yellow_Elephant.svg',
    p4Knight: './images/Yellow_Knight.svg',
    rotatingLoader: './images/rotatingLoader.gif',
    backgroundImg: './images/old_paper_background.jpg'
};

function loadImages(sources, callback) {
    var images = {};
    var loadedImages = 0;
    var numImages = 0;
    var x;
    for (x in sources) {
        if (sources.hasOwnProperty(x)) {
            numImages++;
        }
    }
    for (x in sources) {
        if (sources.hasOwnProperty(x)) {
            images[x] = new Image();
            images[x].onload = function () {
                if (++loadedImages >= numImages) {
                    x = null;
                    callback(images);
                }
            };
            images[x].src = sources[x];
        }
    }
}



function initStage(images) {
	var CANVAS_SIZE = parseInt((window.innerHeight < window.innerWidth ? window.innerHeight : window.innerWidth) * 0.95, 10); //10 ensures decimal (not octal etc.)
    CANVAS_SIZE = CANVAS_SIZE - CANVAS_SIZE % 12; //ensure canvas_size is multiple of 12
    var SQUARES = 64;
    var SQUARES_ON_ROW = 8;
    var SQUARE_SIZE = CANVAS_SIZE / 12;
    var BOARD_BORDER = 2 * SQUARE_SIZE;
    var BOARD_COLOUR = 'GoldenRod'; //    var BOARD_COLOUR = '#F7F2E0'; 
    var BOARD_LINE_COLOUR = '#252500'; //'#331A00';
    var SCORE_PANEL_HIGHLIGHT_COLOUR = '#331A00';
    var BOARD_LINE_STROKEWIDTH = CANVAS_SIZE / 320; // Math.ceil(CANVAS_SIZE / 200);
    //var BOARD_HIGHLIGHT_COLOUR = 'grey'; // Now using player colours instead.

    var PIECE_SCALE = 0.8 * SQUARE_SIZE;
    var ROTATION_OFFSET = CANVAS_SIZE / 2;

    var MOVE_TRANSITION_SPEED = 4; //square lengths per second
    var MESSAGE_BOX_SHOW_TIME = 5; //seconds
    var THIS_PLAYER = null; //player number (1,2,3 or 4) after start game msg
    var PLAYER_NAMES = null; //array of player name strings after start game msg
    var PLAYER_COLOURS = ['red', 'green', 'DodgerBlue', 'yellow'];
    var PLAYER_HIGHLIGHT_COLOURS = ['#FFA595', '#B3FFA0', '#BAD6FF', '#F3DE8F'];
    //	var PLAYER_HIGHLIGHT_COLOURS = ['red', 'green', 'blue', 'yellow'];
    var PLAYER_SCORE_TEXT_COLOURS = ['white', 'white', 'white', 'black'];

    // dumbledore is defined in board_style.css
    var PREGAME_NUMBER_FONT = 'dumbledore';
    var PREGAME_NAME_FONT = 'dumbledore';
    var SCORE_PANEL_NAME_FONT = 'dumbledore';
    var SCORE_PANEL_SCORE_FONT = 'dumbledore';
    var MESSAGE_BOX_FONT = 'dumbledore';


    //global variables initialised on loading
    var selectedFromSq = null;
    var selectedToSq = null;
    var highlightedSqs = [];
    var playerTurn = 1; //initially player 1's turn
    var playersOutList = []; //initially all players are not out (empty array)
    var takenPieceCounts = [0, 0, 0, 0]; //initially all players have taken no pieces from the opposition
    var graveyardWidths = [0, 0, 0, 0];
    var graveyardHeights = [0, 0, 0, 0];
    var suddenDeathFlag = false; //becomes true when sudden death is initiated
    var listeningForClicks = true;
    var pieceIsMoving = false;
    var msgDisplayFlag = false;
    
    var stage = new Kinetic.Stage({
        container: 'board',
        width: CANVAS_SIZE,
        height: CANVAS_SIZE
    });
    var backgroundLayer = new Kinetic.Layer();
    var background = new Kinetic.Rect({
        x: 1,
        y: 1,
        width: CANVAS_SIZE - 3,
        height: CANVAS_SIZE - 3,
        fillPatternImage: images.backgroundImg,
        stroke: 'black',
        strokeWidth: 1
    });
    var preGameLayer = new Kinetic.Layer();

    var pC = 1; //pC = playersConnected - initialised to 1 (minimum will be just this player)
    var preConnectedPlayers = [];
    
    var SOCKET;
    var MSG_BUFFER = [];
    
    var boardLayer = new Kinetic.Layer();

    var boardSquareGroup = new Kinetic.Group({
        x: CANVAS_SIZE / 2,
        y: CANVAS_SIZE / 2
    });

    var boardSquares = [];

    var pieceLayer = new Kinetic.Layer();

    var piecesGroup = new Kinetic.Group({
        x: CANVAS_SIZE / 2,
        y: CANVAS_SIZE / 2
    });

    var pieces = [];

    var scorePanelLayer = new Kinetic.Layer();

    var msgLayer = new Kinetic.Layer();
    var msgBoxes = [];
    //var msgText;
    //var msgBackground;

  //**************************************************
    //BoardSquare class
    //**************************************************
    var BoardSquare = function (x_var, y_var, id_var) {
            this.boardSquareId = id_var;
            this.pieceOnId = null; //the id of the piece on this square or null
            this.square_image = new Kinetic.Rect({
                x: x_var,
                y: y_var,
                width: SQUARE_SIZE,
                height: SQUARE_SIZE,
                fill: BOARD_COLOUR,
                stroke: BOARD_LINE_COLOUR,
                strokeWidth: BOARD_LINE_STROKEWIDTH,
                //			opacity: 0.8,
                name: 'sq',
                id: id_var
            });

            this.square_image.on("click tap", function () {
                document.body.style.cursor = 'auto';
                squareClicked(this.getId());
            });
            this.square_image.on('mouseover', function () {
                document.body.style.cursor = 'pointer';
            });
            this.square_image.on('mouseout', function () {
                document.body.style.cursor = 'auto';
            });
        };

    //**************************************************
    //END boardSquare class
    //**************************************************
    
    //**************************************************
    // initialiseSquares
    //**************************************************

    function initialiseSquares() {
        var newSquare;
        var i;
        var rank_no;
        var rank;
        var file;
        for (i = 0; i < SQUARES; i++) {
            rank_no = (i % SQUARES_ON_ROW) + 1; //rank_no: A=1, B=2...
            rank = String.fromCharCode(64 + rank_no); //rank is A-H (left-to-right)
            file = (Math.floor(i / SQUARES_ON_ROW)) + 1; //file is 1-8 (bottom-to-top)
            newSquare = new BoardSquare((rank_no - 1) * SQUARE_SIZE - ROTATION_OFFSET + BOARD_BORDER, //x
            (SQUARES_ON_ROW - file) * SQUARE_SIZE - ROTATION_OFFSET + BOARD_BORDER, //y
            rank + file.toString()); //id, boardSquare_name
            boardSquareGroup.add(newSquare.square_image);
            boardSquares[i] = newSquare;
            newSquare = null;
            rank_no = null;
            rank = null;
            file = null;
        }
    }


    //**************************************************
    // END initialiseSquares
    //**************************************************
    
    //**************************************************
    //preGame functions
    //**************************************************
    //FUNCTION: getPreGameInfo - gameId, preConnectedPlayers
    //**************************************************

    var gameId;
    function getPreGameInfo() {
    	gameId = game_info.gameID; //game_info initiated in jsp file
        var namesArray = game_info.userNames;
        var i;
        for (i = 0; i < namesArray.length; i++) {
            if (namesArray[i] !== null) {
                preConnectedPlayers.push(namesArray[i]);
            }
        }

        //need to get the gameId and preconnectedPlayers from game_info and add to the above variables
        namesArray = null;
        //game_info = null;
    }


    //FUNCTION: addConnectedPlayer
    //**************************************************

    function addConnectedPlayer(name) {
        if (pC < 5) {
            var newPlayer = new Kinetic.Text({
                x: (((pC - 1) % 2) + 1) * CANVAS_SIZE / 3,
                y: (parseInt((pC - 1) / 2, 10) + 1) * CANVAS_SIZE / 3,
                //10 ensures decimal radix (not octal)
                text: name,
                fontSize: SQUARE_SIZE * 3 / 5,
                //parseInt(SQUARE_SIZE / 3, 10), //10 ensures decimal radix (not octal)
                fontFamily: PREGAME_NAME_FONT,
                fill: 'black',
                width: CANVAS_SIZE / 3,
                padding: SQUARE_SIZE / 5,
                offset: [CANVAS_SIZE / 6, 0],
                align: 'center',
                id: name + 'nP'
            });
            pC++;
            preGameLayer.add(newPlayer);
            newPlayer = null;
        }
        //alert("Added player: " + name);
        if (pC <= preConnectedPlayers.length) { //recursively initiate preConnectedPlayers
            addConnectedPlayer(preConnectedPlayers[pC - 1]);
        } else { //update the screen as a new player has joined this game
            preGameLayer.draw();
        }
    }


    //FUNCTION: initialisePregame
    //**************************************************

    function initialisePregame() {
        var gameNoText = new Kinetic.Text({
            x: CANVAS_SIZE / 2,
            y: CANVAS_SIZE / 32,
            text: 'Game: ' + gameId,
            fontSize: SQUARE_SIZE * 3 / 5,
            //parseInt(SQUARE_SIZE / 2, 10), //10 ensures decimal radix (not octal)
            fontFamily: PREGAME_NUMBER_FONT,
            fill: 'black',
            width: CANVAS_SIZE / 3,
            padding: SQUARE_SIZE / 5,
            offset: [CANVAS_SIZE / 6, 0],
            align: 'center',
            id: 'gameIdText'
        });
        preGameLayer.add(gameNoText);



        var spriteLoaderImgCount = 10;

        var animations = {
            idle: []
        };
        var i;
        for (i = 0; i < spriteLoaderImgCount; i++) {
            animations.idle.push({
                x: i * images.rotatingLoader.width / spriteLoaderImgCount,
                y: 0,
                width: images.rotatingLoader.width / spriteLoaderImgCount,
                height: images.rotatingLoader.height
            });
        }


        var loadingIcon = new Kinetic.Sprite({
            x: CANVAS_SIZE / 2,
            y: gameNoText.getHeight() + gameNoText.getY(),
            height: images.rotatingLoader.height,
            width: images.rotatingLoader.width / spriteLoaderImgCount,
            image: images.rotatingLoader,
            animation: 'idle',
            animations: animations,
            frameRate: 10,
            scale: 0.4
        });
        loadingIcon.setOffset(loadingIcon.getHeight() / 2, 0);
        preGameLayer.add(loadingIcon);
        loadingIcon.start();
        loadingIcon = null;
        gameNoText = null;

        //start recursively initialising preConnectedPlayers
        if (preConnectedPlayers.length > 0) {
            addConnectedPlayer(preConnectedPlayers[pC - 1]);
        }

    }


    //FUNCTION: preGameToStartGame
    //**************************************************

    function preGameToStartGame() {
        var gameIdTextObj = preGameLayer.get('#gameIdText')[0];
        gameIdTextObj.moveTo(backgroundLayer);

        preGameLayer.remove();

        gameIdTextObj.setOffset(0, 0);
        gameIdTextObj.setX(0);
        gameIdTextObj.setY(0);
        gameIdTextObj.setOpacity(0.4);
        backgroundLayer.draw();
        gameIdTextObj = null;

        initialiseSquares();

        boardLayer.add(boardSquareGroup);

        initialisePieces();

        pieceLayer.add(piecesGroup);

        initialiseBoardRotation();

        initialiseScorePanels();

        boardLayer.draw();
        pieceLayer.draw();

        stage.add(boardLayer);
        stage.add(pieceLayer);
        stage.add(scorePanelLayer);

        highlightPanel(playerTurn);
        scorePanelLayer.draw();

        msgAlert("Welcome " + myName + "!\n" + "You are player " + THIS_PLAYER + ".\n" + PLAYER_NAMES[0] + " is player 1 and moves first.\n");


        ///Only to test
        if (TEST_MODE) {
            runTests();
        }
        ///testing only
    }

    //**************************************************
    //END preGame functions
    //**************************************************

    function msgAlert(msgString) {
        var newMsgBox = new msgBox();

        newMsgBox.msgText = new Kinetic.Text({
            x: BOARD_BORDER + (4 * SQUARE_SIZE),
            y: BOARD_BORDER + (4 * SQUARE_SIZE),
            text: msgString,
            fontSize: SQUARE_SIZE * 3 / 5,
            fontFamily: MESSAGE_BOX_FONT,
            fill: '#555',
            width: 10 * SQUARE_SIZE,
            padding: SQUARE_SIZE / 5,
            align: 'center',
            id: "msgText" + msgBoxes.length
        });
        newMsgBox.msgText.setOffset(newMsgBox.msgText.getWidth() / 2, newMsgBox.msgText.getHeight() / 2);

        newMsgBox.msgBackground = new Kinetic.Rect({
            x: newMsgBox.msgText.getX(),
            y: newMsgBox.msgText.getY(),
            stroke: '#555',
            strokeWidth: 5,
            fill: '#ddd',
            width: newMsgBox.msgText.getWidth(),
            height: newMsgBox.msgText.getHeight(),
            shadowColor: 'black',
            shadowBlur: 10,
            shadowOffset: [10, 10],
            shadowOpacity: 0.5,
            cornerRadius: 10
        });
        newMsgBox.msgBackground.setOffset(newMsgBox.msgText.getOffset());

        newMsgBox.msgBackground.setScale(0.001);
        newMsgBox.msgText.setScale(0.001);

        msgLayer.add(newMsgBox.msgBackground);
        msgLayer.add(newMsgBox.msgText);
        stage.add(msgLayer);

//        newMsgBox.msgBackground.transitionTo({
//            x: BOARD_BORDER + (4 * SQUARE_SIZE),
//            y: BOARD_BORDER + (4 * SQUARE_SIZE),
//            scale: {
//                x: 1,
//                y: 1
//            },
//            duration: 1,
//            easing: 'back-ease-out'
//        });
//
//        newMsgBox.msgText.transitionTo({
//            x: BOARD_BORDER + (4 * SQUARE_SIZE),
//            y: BOARD_BORDER + (4 * SQUARE_SIZE),
//            scale: {
//                x: 1,
//                y: 1
//            },
//            duration: 1,
//            easing: 'back-ease-out'
//        });
       
        msgBackground_tween = new Kinetic.Tween({
            node: newMsgBox.msgBackground,
            x: BOARD_BORDER + (4 * SQUARE_SIZE),
            y: BOARD_BORDER + (4 * SQUARE_SIZE),
            scaleX: 1,
	    scaleY: 1,
            duration: 1,
            easing: Kinetic.Easings.BackEaseOut
        });

        msgText_tween = new Kinetic.Tween({
            node: newMsgBox.msgText,
            x: BOARD_BORDER + (4 * SQUARE_SIZE),
            y: BOARD_BORDER + (4 * SQUARE_SIZE),
            scaleX: 1,
            scaleY: 1,
            duration: 1,
            easing: Kinetic.Easings.BackEaseOut,
	    onFinish: function() {
		newMsgBox.msgText.on('mouseover', function () {
		    document.body.style.cursor = 'pointer';
		});
		newMsgBox.msgText.on('mouseout', function () {
		    document.body.style.cursor = 'auto';
		});
		newMsgBox.msgText.on("click tap", function () {
		    msgTextClicked(this.getId());
		});
		window.setTimeout(msgTextClicked, 1000 * MESSAGE_BOX_SHOW_TIME, newMsgBox.msgText.getId());
	    }
        });

        msgBoxes.push(newMsgBox);

        msgDisplayFlag = true;
        
        msgBackground_tween.play();
        msgText_tween.play();
                
    }

    function msgTextClicked(msgText_ID) {
        var clickedMsgBox = getMsgBoxFromMsgTextId(msgText_ID);
        clickedMsgBox.msgText.off("click tap mouseover mouseout");
        document.body.style.cursor = 'auto';
        msgText_tween = new Kinetic.Tween({
            node: clickedMsgBox.msgText,
            scaleX: 0.001,
            scaleY: 0.001,
            duration: 1,
            easing: Kinetic.Easings.BackEaseIn
        });
        msgBackground_tween = new Kinetic.Tween({
            node: clickedMsgBox.msgBackground,
            scaleX: 0.001,
            scaleY: 0.001,
            duration: 1,
            easing: Kinetic.Easings.BackEaseIn,
            onFinish: function () {
		//alert("tween finished");
		clickedMsgBox.msgText.hide();
		clickedMsgBox.msgBackground.hide();
		clickedMsgBox = null;
		window.setTimeout(function() {
		    msgDisplayFlag = false;
                    process_a_msg();
		},200);            }
        });
        msgBackground_tween.play();
	msgText_tween.play();
        
//        clickedMsgBox.msgText.tween.reverse();
//            
//        clickedMsgBox.msgBackground.tween.reverse();            
//             
        
        //clickedMsgBox = null;
        //msgDisplayFlag = false;
    }


    function getMsgBoxFromMsgTextId(msgText_id) {
    	var i;
        for (i = 0; i < msgBoxes.length; i++) {
            if (msgBoxes[i].msgText.getId() === msgText_id) {
                return msgBoxes[i];
            }
        }
    }


    //**************************************************
    //msgBox class
    //**************************************************
    var msgBox = function () {
            this.msgText;
            this.msgBackground;
        };


    //**************************************************
    //END msgBox class
    //**************************************************
    
    //**************************************************
    //Piece class
    //**************************************************
    var Piece = function (player_var, type_var, pcNumber_var, image_var) {
            var temp_id_var = 'p' + player_var + type_var + pcNumber_var; //e.g. p2pawn3
            this.player = player_var;
            this.type = type_var;
            this.pieceId = temp_id_var;
            this.onSquareId = null; //square id that the piece is on or null
            this.piece_image = new Kinetic.Image({
                image: image_var,
                scale: parseInt(1000 * PIECE_SCALE / (image_var.height > image_var.width ? image_var.height : image_var.width), 10) / 1000,
                rotation: 0,
                name: 'pc',
                id: temp_id_var //e.g. p2pawn3
            });
            temp_id_var = null;

            this.piece_image.on("click tap", function () {
                document.body.style.cursor = 'auto';
                pieceClicked(this.getId());
            });
            this.piece_image.on('mouseover', function () {
                document.body.style.cursor = 'pointer';
            });
            this.piece_image.on('mouseout', function () {
                document.body.style.cursor = 'auto';
            });
        };

    Piece.prototype = {
        initialiseOnSquareId: function (sqID_var) {
            this.onSquareId = sqID_var;
            this.square_image = boardSquareGroup.get('#' + sqID_var)[0];
            this.piece_image.setOffset(this.piece_image.getWidth() / 2, this.piece_image.getHeight() / 2);

            this.piece_image.setX(boardSquareGroup.get('#' + sqID_var)[0].getX() + (SQUARE_SIZE / 2));

            this.piece_image.setY(boardSquareGroup.get('#' + sqID_var)[0].getY() + (SQUARE_SIZE / 2));

            getSquareFromSquareId(sqID_var).pieceOnId = this.pieceId;
        }
    };


    //**************************************************
    //END Piece class
    //**************************************************

    //**************************************************
    // initialisePieces
    //**************************************************

    function initialisePieces() {
        var pcInit = {
            p1P: {
                player: 1,
                type: 'pawn',
                image: images.p1Pawn,
                sqs: ['G1', 'G2', 'G3', 'G4']
            },
            p1B: {
                player: 1,
                type: 'boat',
                image: images.p1Boat,
                sqs: ['H1']
            },
            p1N: {
                player: 1,
                type: 'knight',
                image: images.p1Knight,
                sqs: ['H2']
            },
            p1E: {
                player: 1,
                type: 'elephant',
                image: images.p1Elephant,
                sqs: ['H3']
            },
            p1K: {
                player: 1,
                type: 'king',
                image: images.p1King,
                sqs: ['H4']
            },

            p2P: {
                player: 2,
                type: 'pawn',
                image: images.p2Pawn,
                sqs: ['A2', 'B2', 'C2', 'D2']
            },
            p2B: {
                player: 2,
                type: 'boat',
                image: images.p2Boat,
                sqs: ['A1']
            },
            p2N: {
                player: 2,
                type: 'knight',
                image: images.p2Knight,
                sqs: ['B1']
            },
            p2E: {
                player: 2,
                type: 'elephant',
                image: images.p2Elephant,
                sqs: ['C1']
            },
            p2K: {
                player: 2,
                type: 'king',
                image: images.p2King,
                sqs: ['D1']
            },

            p3P: {
                player: 3,
                type: 'pawn',
                image: images.p3Pawn,
                sqs: ['B5', 'B6', 'B7', 'B8']
            },
            p3B: {
                player: 3,
                type: 'boat',
                image: images.p3Boat,
                sqs: ['A8']
            },
            p3N: {
                player: 3,
                type: 'knight',
                image: images.p3Knight,
                sqs: ['A7']
            },
            p3E: {
                player: 3,
                type: 'elephant',
                image: images.p3Elephant,
                sqs: ['A6']
            },
            p3K: {
                player: 3,
                type: 'king',
                image: images.p3King,
                sqs: ['A5']
            },

            p4P: {
                player: 4,
                type: 'pawn',
                image: images.p4Pawn,
                sqs: ['E7', 'F7', 'G7', 'H7']
            },
            p4B: {
                player: 4,
                type: 'boat',
                image: images.p4Boat,
                sqs: ['H8']
            },
            p4N: {
                player: 4,
                type: 'knight',
                image: images.p4Knight,
                sqs: ['G8']
            },
            p4E: {
                player: 4,
                type: 'elephant',
                image: images.p4Elephant,
                sqs: ['F8']
            },
            p4K: {
                player: 4,
                type: 'king',
                image: images.p4King,
                sqs: ['E8']
            }
        };

        var newPiece;
        var j = 0;
        var pc;
        var i;
        for (pc in pcInit) {
        	if (pcInit.hasOwnProperty(pc)) {
        		for (i = 0; i < pcInit[pc].sqs.length; i++) {
        			newPiece = new Piece(pcInit[pc].player, pcInit[pc].type, i + 1, pcInit[pc].image);
        			newPiece.initialiseOnSquareId(pcInit[pc].sqs[i]);
        			piecesGroup.add(newPiece.piece_image);
        			pieces[j] = newPiece;
        			newPiece = null;
        			j++;
        		}
        	}
        }
        j = null;
    }

    //**************************************************
    // END initialisePieces
    //**************************************************
    //**************************************************
    // initialiseBoardRotation
    //**************************************************

    function initialiseBoardRotation() {
        if (THIS_PLAYER !== null) {
            var degrees = (2 - THIS_PLAYER) * 90;
            boardSquareGroup.rotateDeg(degrees);
            piecesGroup.rotateDeg(degrees);
            var i;
            for (i = 0; i < pieces.length; i++) {
                pieces[i].piece_image.rotateDeg(-1 * degrees); //135 degrees? doesn't make sense but it's the only way it works
            }
            degrees = null;
        }
    }

    //**************************************************
    // END initialiseBoardRotation
    //**************************************************
    //**************************************************
    // initialiseScorePanels
    //**************************************************

    function initialiseScorePanels() {
        var left_edge = -1; //units of SQUARE_SIZE, will be added to BOARD_BORDER to shape vertices
        var boardPanelGap = 0.1;
        var panel_height = 2;
        var panel_width = 4.5;

        //store the values to initialise the panel shapes and locations
        var pA = [left_edge, //-1
        - 1 * boardPanelGap, //-0.1
        left_edge + panel_width, //3.5
        8 - left_edge - panel_width, //4.5
        8 + boardPanelGap, //8.1
        8 - left_edge, //9
        left_edge + panel_height, //1
        8 - left_edge - panel_height]; //7
        var bottomPanel = [pA[0], pA[7], //bottom panel points array
        pA[1], pA[7], pA[1], pA[4], pA[2], pA[4], //first 6 sets of points are the panel vertices
        pA[2], pA[5], pA[0], pA[5], pA[0], pA[4], //last 2 sets are the name and score text locations
        pA[0], pA[7]];

        var leftPanel = [pA[0], pA[0], //left panel points array
        pA[2], pA[0], pA[2], pA[1], pA[1], pA[1], pA[1], pA[6], pA[0], pA[6], pA[0], pA[0], pA[0], pA[1]];

        var topPanel = [pA[3], pA[0], //top panel points array
        pA[5], pA[0], pA[5], pA[6], pA[4], pA[6], pA[4], pA[1], pA[3], pA[1], pA[3], pA[0], pA[4], pA[1]];

        var rightPanel = [pA[3], pA[4], //right panel points array
        pA[4], pA[4], pA[4], pA[7], pA[5], pA[7], pA[5], pA[5], pA[3], pA[5], pA[3], pA[4], pA[4], pA[7]];



        var player = (THIS_PLAYER !== null ? THIS_PLAYER : 2); //player 2 at bottom if all AI game
        var panelArrays = [bottomPanel, leftPanel, topPanel, rightPanel];
     
        var i, x, panel, name, score;
        for (i = 0; i < 4; i++) {
            x = panelArrays[i];
            panel = new Kinetic.Polygon({
                points: [BOARD_BORDER + (x[0] * SQUARE_SIZE), BOARD_BORDER + (x[1] * SQUARE_SIZE), BOARD_BORDER + (x[2] * SQUARE_SIZE), BOARD_BORDER + (x[3] * SQUARE_SIZE), BOARD_BORDER + (x[4] * SQUARE_SIZE), BOARD_BORDER + (x[5] * SQUARE_SIZE), BOARD_BORDER + (x[6] * SQUARE_SIZE), BOARD_BORDER + (x[7] * SQUARE_SIZE), BOARD_BORDER + (x[8] * SQUARE_SIZE), BOARD_BORDER + (x[9] * SQUARE_SIZE), BOARD_BORDER + (x[10] * SQUARE_SIZE), BOARD_BORDER + (x[11] * SQUARE_SIZE)],
                fill: PLAYER_COLOURS[player - 1],
                stroke: BOARD_LINE_COLOUR,
                strokeWidth: BOARD_LINE_STROKEWIDTH / 4,
                id: 'p' + player + 'panel'
            });
            name = new Kinetic.Text({
                x: BOARD_BORDER + (x[12] * SQUARE_SIZE),
                y: BOARD_BORDER + (x[13] * SQUARE_SIZE),
                text: PLAYER_NAMES[player - 1],
                fontSize: SQUARE_SIZE * 3 / 5,
                fontFamily: SCORE_PANEL_NAME_FONT,
                fill: PLAYER_SCORE_TEXT_COLOURS[player - 1],
                width: 4.5 * SQUARE_SIZE,
                padding: SQUARE_SIZE / 5,
                align: 'center',
                id: 'p' + player + 'panelName'
            });
            score = new Kinetic.Text({
                x: BOARD_BORDER + ((x[14]-0.15) * SQUARE_SIZE),
		//0.15 added due to the increase from 0.9 to 1.2 below
                y: BOARD_BORDER + (x[15] * SQUARE_SIZE),
                text: 0,
                fontSize: SQUARE_SIZE * 3 / 5,
                fontFamily: SCORE_PANEL_SCORE_FONT,
                fill: PLAYER_SCORE_TEXT_COLOURS[player - 1],
                width: 1.2 * SQUARE_SIZE, 
		//originally 0.9 but raised to 1.2 to prevent double digits falling onto new line
                padding: SQUARE_SIZE / 5,
                align: 'center',
                id: 'p' + player + 'panelScore'
            });

            scorePanelLayer.add(panel);
            scorePanelLayer.add(name);
            scorePanelLayer.add(score);

            player = (player % 4) + 1;
        }
        player = null;
    }

    //**************************************************
    // END initialiseScorePanels
    //**************************************************

    //**************************************************
    // In-game functions
    //**************************************************

    function playerQuit(playerNo, newID) {
        msgAlert(PLAYER_NAMES[playerNo - 1] + " has quit!\n" + newID + " is taking over.");
        scorePanelLayer.get('#' + 'p' + playerNo + 'panelName')[0].setText(newID);
        scorePanelLayer.draw();
    }

    function boatTriumph(triumphantBoat, deadBoats) {
        var triumphantPlayer = getPieceFromPieceId(getSquareFromSquareId(triumphantBoat).pieceOnId).player;
        var i;
        for (i = 0; i < deadBoats.length; i++) {
            takePiece(deadBoats[i], triumphantPlayer);
            updatePieceOn(deadBoats[i], null);
        }
        msgAlert("BOAT TRIUMPH\n" + PLAYER_NAMES[triumphantPlayer - 1] + " takes the other three boats and scores 6 points!");
    }

    function pawnPromotion(sq, pieceType) {
        var promotingPlayer = getPieceFromPieceId(getSquareFromSquareId(sq).pieceOnId).player;
        takePiece(sq, null);
        //alert("PP: taken piece");
        var promotingToPiece = getPieceFromPieceId('p' + promotingPlayer + pieceType + '1');
        //alert("promotingToPiece visibility should be false. It is: " +promotingToPiece.piece_image.getVisible()); 
        promotingToPiece.piece_image.setX(boardSquareGroup.get('#' + sq)[0].getX() + SQUARE_SIZE / 2);
        promotingToPiece.piece_image.setY(boardSquareGroup.get('#' + sq)[0].getY() + SQUARE_SIZE / 2);
        updateOnSquare(promotingToPiece.pieceId, sq);
        updatePieceOn(sq, promotingToPiece.pieceId);
        promotingToPiece.piece_image.setVisible(true);
        pieceLayer.draw();
/*
        for (var i = 0; i < pieces.length; i++) {
            if (pieces[i].player === promotingPlayer && pieces[i].type === pieceType && pieces[i].onSquareId === null) {
		//alert("Found piece to replace the pawn");
                pieces[i].piece_image.setX(boardSquareGroup.get('#' + sq)[0].getX() + SQUARE_SIZE / 2);
                pieces[i].piece_image.setY(boardSquareGroup.get('#' + sq)[0].getY() + SQUARE_SIZE / 2);
                updateOnSquare(pieces[i].pieceId, sq);
                updatePieceOn(sq, pieces[i].pieceId);
                pieces[i].piece_image.show();
                pieceLayer.draw();
		//alert("Pawn promotion finished");
                break;
            }
        }
		 */
        msgAlert("PAWN PROMOTION\n" + PLAYER_NAMES[promotingPlayer - 1] + "'s pawn has been promoted to a " + pieceType + "!");
    }

    function suddenDeath(moveLimit) {
        //alert("Inside sudden death with move limit: " + moveLimit);
        var remainingPlayers = [];
        var player;
        for (player = 1; player <= 4; player++) {
            if (!isPlayerOut(player)) {
                createMoveLimitDisplay(player, moveLimit);
                remainingPlayers.push(player);
            }
        }
        scorePanelLayer.draw();
        suddenDeathFlag = true;
        msgAlert("SUDDEN DEATH\n" + PLAYER_NAMES[remainingPlayers[0] - 1] + " and " + PLAYER_NAMES[remainingPlayers[1] - 1] + " each have " + moveLimit + " moves left!");

    }

    function decrementMoveLimit(player) {
        scorePanelLayer.get('#' + 'p' + player + 'moveLimit')[0].setText(
        scorePanelLayer.get('#' + 'p' + player + 'moveLimit')[0].getText() - 1);
        scorePanelLayer.draw();
    }

    function createMoveLimitDisplay(player, moveLimit) {
        //alert("Inside createMoveLimitDisplayer with player: " + player + " and moveLimit: " + moveLimit);
        var newDisplay = new Kinetic.Text({
            x: scorePanelLayer.get('#' + 'p' + player + 'panelScore')[0].getX(),
	    y: (scorePanelLayer.get('#' + 'p' + player + 'panelScore')[0].getY() < CANVAS_SIZE / 2 ? scorePanelLayer.get('#' + 'p' + player + 'panelScore')[0].getY() + SQUARE_SIZE : scorePanelLayer.get('#' + 'p' + player + 'panelScore')[0].getY() - SQUARE_SIZE),
            text: moveLimit,
            fontSize: SQUARE_SIZE * 3 / 5,
            fontFamily: SCORE_PANEL_SCORE_FONT,
            fontStyle: 'bold',
            fill: 'red',
	    width: scorePanelLayer.get('#' + 'p' + player + 'panelScore')[0].getWidth(),
            padding: SQUARE_SIZE / 5,
            align: 'center',
            id: 'p' + player + 'moveLimit'
        });
        scorePanelLayer.add(newDisplay);
        newDisplay = null;
    }

    function setScorePanelFlashing(playerNo) {
        var flashTime = 0;
        var anim = new Kinetic.Animation(function (frame) {
            if (frame.time > (flashTime + 500)) {
                flashTime = frame.time;
                unhighlightPanel(playerNo);
            } else if (frame.time > (flashTime + 250)) {
                highlightPanel(playerNo);
            }
        }, scorePanelLayer);

        anim.start();
    }

    function endGame(outcome, playerArray) { //playerArray has one element if win or more if draw
        var i;
    	unhighlightPanel(playerTurn);
        if (outcome === 'win') {
            msgAlert("GAME OVER\n" + PLAYER_NAMES[playerArray[0] - 1] + " wins!");
        } else if (outcome === 'draw') {
            var drawingPlayersString = PLAYER_NAMES[playerArray[0] - 1];
            for (i = 1; i < playerArray.length; i++) {
                if (i === playerArray.length - 1) {
                    drawingPlayersString += ' and ';
                } else {
                    drawingPlayersString += ', ';
                }
                drawingPlayersString += PLAYER_NAMES[playerArray[i] - 1];
            }
            msgAlert("GAME OVER\n" + "The game ended in a draw between " + drawingPlayersString + "!");
        }
        for (i = 0; i < playerArray.length; i++) {
            setScorePanelFlashing(playerArray[i]);
        }
        //document.getElementById('returnButton').style.top = SQUARE_SIZE * -6 + "pt";
        //window.setTimeout("document.getElementById('returnButton').style.display = 'block'", 5000);
    }

    function playerOut(playerNo) {
        if (!isPlayerOut(playerNo)) {
            playersOutList.push(playerNo);
        }
        fadeScorePanel(playerNo);
        if (playerTurn === playerNo) {
            nextPlayerTurn();
        }
        msgAlert("PLAYER OUT\n" + PLAYER_NAMES[playerNo - 1] + " is out of the game!");


    }

    function fadeScorePanel(playerNo) {
        scorePanelLayer.get('#' + 'p' + playerNo + 'panel')[0].setOpacity(0.4);
        scorePanelLayer.get('#' + 'p' + playerNo + 'panelName')[0].setOpacity(0.4);
        scorePanelLayer.get('#' + 'p' + playerNo + 'panelScore')[0].setOpacity(0.4);
        scorePanelLayer.draw();
    }

    function updateScores(scoreArray) {
    	var i;
        for (i = 0; i < scoreArray.length; i++) {
            scorePanelLayer.get('#' + 'p' + (i + 1) + 'panelScore')[0].setText(scoreArray[i]);
        }
        scorePanelLayer.draw();
    }


    function isPlayerOut(playerNo) {
    	var i;
        for (i = 0; i < playersOutList.length; i++) {
            if (playersOutList[i] === playerNo) {
                return true;
            }
        }
        return false;
    }

    function nextPlayerTurn() {
        unhighlightPanel(playerTurn);
        var temp = playerTurn;
        temp++;
        if (temp > 4) {
            temp = 1;
        }
        while (isPlayerOut(temp)) {
            temp++;
            if (temp > 4) {
                temp = 1;
            }
        }
        playerTurn = temp;
        highlightPanel(playerTurn);
        scorePanelLayer.draw();
        temp = null;
        //alert("It is now the turn of player: " + playerTurn);
    }

    function unhighlightPanel(playerNo) {
        scorePanelLayer.get('#' + 'p' + playerNo + 'panel')[0].setStroke(BOARD_LINE_COLOUR);
        scorePanelLayer.get('#' + 'p' + playerNo + 'panel')[0].setStrokeWidth(BOARD_LINE_STROKEWIDTH / 4);
        scorePanelLayer.get('#' + 'p' + playerNo + 'panelName')[0].setFontStyle('normal');
        scorePanelLayer.get('#' + 'p' + playerNo + 'panelScore')[0].setFontStyle('normal');
    }

    function highlightPanel(playerNo) {
        scorePanelLayer.get('#' + 'p' + playerNo + 'panel')[0].setStroke(SCORE_PANEL_HIGHLIGHT_COLOUR);
        scorePanelLayer.get('#' + 'p' + playerNo + 'panel')[0].setStrokeWidth(BOARD_LINE_STROKEWIDTH * 2);
        scorePanelLayer.get('#' + 'p' + playerNo + 'panelName')[0].setFontStyle('bold');
        scorePanelLayer.get('#' + 'p' + playerNo + 'panelScore')[0].setFontStyle('bold');
    }

    function getSquareFromSquareId(sq_id) {
    	var i;
        for (i = 0; i < boardSquares.length; i++) {
            if (boardSquares[i].boardSquareId === sq_id) {
                return boardSquares[i];
            }
        }
    }

    function getPieceFromPieceId(pc_id) {
    	var i;
        for (i = 0; i < pieces.length; i++) {
            if (pieces[i].pieceId === pc_id) {
                return pieces[i];
            }
        }
    }

    function highlightSq(sqID) {
        highlightedSqs.push(sqID);
        boardSquareGroup.get('#' + sqID)[0].setFill(PLAYER_HIGHLIGHT_COLOURS[playerTurn - 1]);
        //		BOARD_HIGHLIGHT_COLOUR);
        boardLayer.draw();
    }

    function unhighlightSqs() {
        while (highlightedSqs[0]) {
            boardSquareGroup.get('#' + highlightedSqs[0])[0].setFill(BOARD_COLOUR);
            highlightedSqs.shift();
        }
        boardLayer.draw();
    }

    function removeSelections() {
        unhighlightSqs();
        selectedFromSq = null;
        selectedToSq = null;
    }

    function updateOnSquare(pieceId, squareId) {
        getPieceFromPieceId(pieceId).onSquareId = squareId;
    }

    function updatePieceOn(squareId, pieceId) {
        getSquareFromSquareId(squareId).pieceOnId = pieceId;
    }

    function takePiece(sqId, takingPlayerNo) {
        var takenPieceId = getSquareFromSquareId(sqId).pieceOnId;
        piecesGroup.get('#' + takenPieceId)[0].setVisible(false);
        updateOnSquare(takenPieceId, null);
        pieceLayer.draw();
        if (takingPlayerNo !== null) {
            pieceToGraveyard(takenPieceId, takingPlayerNo);
            takenPieceCounts[takingPlayerNo - 1]++;
        }
    }

    function pieceToGraveyard(takenPieceId, takingPlayerNo) {
        var takenPiece = piecesGroup.get('#' + takenPieceId)[0];
        var scorePanelNameX = scorePanelLayer.get('#' + 'p' + takingPlayerNo + 'panelName')[0].getX();
        var scorePanelNameY = scorePanelLayer.get('#' + 'p' + takingPlayerNo + 'panelName')[0].getY();
        var scorePanelNameWidth = scorePanelLayer.get('#' + 'p' + takingPlayerNo + 'panelName')[0].getWidth();
        var takenPieceImage = new Kinetic.Image({
            image: takenPiece.getImage(),
            scale: 0.8 * takenPiece.getScale().x
        });

        var scaledWidth = (takenPieceImage.getScale().x * takenPieceImage.getWidth());
        if (graveyardWidths[takingPlayerNo - 1] + scaledWidth < scorePanelNameWidth + SQUARE_SIZE) {
            takenPieceImage.setX(scorePanelNameX > CANVAS_SIZE / 2 ? scorePanelNameX + graveyardWidths[takingPlayerNo - 1] : scorePanelNameX + scorePanelNameWidth - graveyardWidths[takingPlayerNo - 1] - scaledWidth);
            takenPieceImage.setY(scorePanelNameY < CANVAS_SIZE / 2 ? scorePanelNameY - 0.8 * SQUARE_SIZE : BOARD_BORDER + 9 * SQUARE_SIZE + 0.1 * SQUARE_SIZE);
            graveyardWidths[takingPlayerNo - 1] += scaledWidth + 0.1 * SQUARE_SIZE;
        } else {
            var scaledHeight = (takenPieceImage.getScale().x * takenPieceImage.getHeight());
            takenPieceImage.setX(scorePanelNameX > CANVAS_SIZE / 2 ? scorePanelNameX + scorePanelNameWidth + SQUARE_SIZE / 2 : scorePanelNameX - SQUARE_SIZE / 2);
            takenPieceImage.setY(scorePanelNameY < CANVAS_SIZE / 2 ? scorePanelNameY + graveyardHeights[takingPlayerNo - 1] : scorePanelNameY + 0.9 * SQUARE_SIZE - graveyardHeights[takingPlayerNo - 1] - scaledHeight);
            graveyardHeights[takingPlayerNo - 1] += scaledHeight + 0.1 * SQUARE_SIZE;
            takenPieceImage.setOffset(takenPieceImage.getWidth() / 2, 0);
        }

        scorePanelLayer.add(takenPieceImage);
        scorePanelLayer.draw();
        takenPiece = null;
        takenPieceImage = null;
    }



    function invalidMove() {
        msgAlert("That's an invalid move");
        removeSelections();
    }


    function moveCompleted(fromSqId, toSqId) {
//	alert(fromSqId + ' ' + toSqId);
        var movedPieceId = getSquareFromSquareId(fromSqId).pieceOnId;
//	alert(getSquareFromSquareId(toSqId).pieceOnId);
        if (getSquareFromSquareId(toSqId).pieceOnId !== null) {
            takePiece(toSqId, getPieceFromPieceId(movedPieceId).player);
        }
        if (suddenDeathFlag) {
            decrementMoveLimit(getPieceFromPieceId(movedPieceId).player);
        }
        updatePieceOn(toSqId, movedPieceId);
        updateOnSquare(movedPieceId, toSqId);
        updatePieceOn(fromSqId, null);
        removeSelections();
        nextPlayerTurn();
        MSG_BUFFER.shift();
        movedPieceId = null;
        //varying_transition_time = MOVE_TRANSITION_TIME;
        pieceIsMoving = false;
        window.setTimeout(process_a_msg,200);
    }

    function movePiece(fromSqId, toSqId) {
        if (!pieceIsMoving) {
            highlightSq(fromSqId);
            highlightSq(toSqId);
        } 
        pieceIsMoving = true;
        var movingPiece = piecesGroup.get('#' + getSquareFromSquareId(fromSqId).pieceOnId)[0];
        movingPiece.moveToTop();
        //piecesGroup.get('#' + getSquareFromSquareId(fromSqId).pieceOnId)[0].moveToTop();
        pieceLayer.draw();
        var moveToX = (boardSquareGroup.get('#' + toSqId)[0].getX() + (SQUARE_SIZE / 2));
        var moveToY = (boardSquareGroup.get('#' + toSqId)[0].getY() + (SQUARE_SIZE / 2));
//        movingPiece.transitionTo({
//            //piecesGroup.get('#' + getSquareFromSquareId(fromSqId).pieceOnId)[0].transitionTo({
//            x: moveToX,
//            //(boardSquareGroup.get('#' + toSqId)[0].getX() + (SQUARE_SIZE / 2)),
//            y: moveToY,
//            //(boardSquareGroup.get('#' + toSqId)[0].getY() + (SQUARE_SIZE / 2)),
//            duration: Math.sqrt(((movingPiece.getX() - moveToX) * (movingPiece.getX() - moveToX) + (movingPiece.getY() - moveToY) * (movingPiece.getY() - moveToY))) / (SQUARE_SIZE * MOVE_TRANSITION_SPEED),
//            //MOVE_TRANSITION_TIME,
//            callback: function () {
//                moveCompleted(fromSqId, toSqId);
//            }
//        });
        var piece_tween = new Kinetic.Tween({
        	node: movingPiece,
        	//piecesGroup.get('#' + getSquareFromSquareId(fromSqId).pieceOnId)[0].transitionTo({
        	x: moveToX,
        	//(boardSquareGroup.get('#' + toSqId)[0].getX() + (SQUARE_SIZE / 2)),
        	y: moveToY,
        	//(boardSquareGroup.get('#' + toSqId)[0].getY() + (SQUARE_SIZE / 2)),
        	duration: Math.sqrt(((movingPiece.getX() - moveToX) * (movingPiece.getX() - moveToX) + (movingPiece.getY() - moveToY) * (movingPiece.getY() - moveToY))) / (SQUARE_SIZE * MOVE_TRANSITION_SPEED),
        	//MOVE_TRANSITION_TIME,
        	onFinish: function () {
		    //alert("Movecompleted timeout");
		    window.setTimeout(moveCompleted,100,fromSqId, toSqId);
        		//moveCompleted(fromSqId, toSqId);
        	}
        });
        piece_tween.play();
        //movingPiece = null;
    }

    function submitMove(fromSqId, toSqId) {
        if (playerTurn !== THIS_PLAYER) {
            msgAlert("It's not your turn");
            removeSelections();
            restoreAllListeners();
        } else {
            var msg = {
                type: 1,
                data: {
                    from_sq: fromSqId,
                    to_sq: toSqId
                }
            };
            //alert("sending" + JSON.stringify(msg));
            SOCKET.send(JSON.stringify(msg));
            msg = null;
        }
    }

    function squareClicked(id_var) {

        process_a_msg();

        var this_sq = getSquareFromSquareId(id_var);
        var pc_on_id = this_sq.pieceOnId;

        if (selectedFromSq === null && pc_on_id !== null && getPieceFromPieceId(pc_on_id).player === THIS_PLAYER) {
            highlightSq(id_var);
            selectedFromSq = id_var;
        } else if (selectedFromSq !== null && selectedToSq === null && (pc_on_id === null || getPieceFromPieceId(pc_on_id).player !== THIS_PLAYER)) {
            highlightSq(id_var);
            selectedToSq = id_var;
            submitMove(selectedFromSq, selectedToSq);
        } else if (selectedFromSq !== null && selectedToSq === null) { //trying to move to a square with one's own piece
            removeSelections();
        }
        if (selectedToSq === null) { //if a move has just been submitted, this fails
            restoreAllListeners();
        }
        this_sq = null;
        pc_on_id = null;
    }

    function pieceClicked(pc_ID) {
        squareClicked(getPieceFromPieceId(pc_ID).onSquareId);
    }


    function restoreAllListeners() {
	if (!listeningForClicks && playerTurn === THIS_PLAYER) {
	    console.log("restoring listeners");
    	    var i;
            for (i = 0; i < boardSquares.length; i++) {
		boardSquares[i].square_image.setListening(true);
            }
            for (i = 0; i < pieces.length; i++) {
		pieces[i].piece_image.setListening(true);
            }
            boardLayer.drawHit();
            pieceLayer.drawHit();
	    listeningForClicks = true;
	}
        
    }

    function disableAllListeners() {
        if (listeningForClicks) {
	    console.log("disabling listeners");
            var i;
            for (i = 0; i < boardSquares.length; i++) {
                boardSquares[i].square_image.setListening(false);
            }
            for (i = 0; i < pieces.length; i++) {
                pieces[i].piece_image.setListening(false);
            }
            boardLayer.drawHit();
            pieceLayer.drawHit();
	    listeningForClicks = false;
        }
        document.body.style.cursor = 'auto';
        
    }


    //**************************************************
    // END In-game functions
    //**************************************************
    //**************************************************
    // process_a_msg
    //**************************************************

    function process_a_msg() {
	console.log("entering process_a_msg");
        if (!msgDisplayFlag && !pieceIsMoving && MSG_BUFFER[0]) {
	    console.log("processing a message");
            disableAllListeners();

            var receivedMsg = JSON.parse(MSG_BUFFER[0]);
	    //alert("Processing a message: " + MSG_BUFFER[0]);
            switch (receivedMsg.type) {
/*    
		      case 1://This is for testing only: client shouldn't receive message type 1
		      movePiece(receivedMsg.data.from_sq,receivedMsg.data.to_sq);
		      break;
		      //remove this after testing
			 */
            case 2:
                movePiece(receivedMsg.data.from_sq, receivedMsg.data.to_sq);
                break;
            case 3:
                invalidMove();
                break;
            case 4:
                updateScores([receivedMsg.data.p1, receivedMsg.data.p2, receivedMsg.data.p3, receivedMsg.data.p4]);
                break;
            case 5:
                playerOut(receivedMsg.data.player);
                break;
            case 6:
                endGame(receivedMsg.data.outcome, receivedMsg.data.player); //player can be array if draw
                break;
            case 7:
                suddenDeath(receivedMsg.data.moveLimit);
                break;
            case 8:
                pawnPromotion(receivedMsg.data.sq, receivedMsg.data.pieceType);
                break;
            case 9:
                addConnectedPlayer(receivedMsg.data.name);
                break;
            case 10:
                PLAYER_NAMES = [receivedMsg.data.p1, receivedMsg.data.p2, receivedMsg.data.p3, receivedMsg.data.p4];
                var i;
                for (i = 0; i < PLAYER_NAMES.length; i++) {
                    if (PLAYER_NAMES[i] === myName) {
                        THIS_PLAYER = i + 1;
                        //alert( "This player named " + myName + " is player " + THIS_PLAYER);
                    }
                }
                //Note: an all AI game will mean that THIS_PLAYER remains null
                preGameToStartGame(); //start the game!
                break;
            case 11:
                boatTriumph(receivedMsg.data.tBoat, receivedMsg.data.boats); //boats is an array of 3 sqs
                break;
            case 13:
                playerQuit(receivedMsg.data.player, receivedMsg.data.newID);
                break;
            default:
		console.log("An unexpected message was received of type: " + receivedMsg.type + "\n");
		console.log(receivedMsg);
            }
            if (receivedMsg.type !== 2) {
                MSG_BUFFER.shift();
                receivedMsg = null;
                process_a_msg();
            }
        }
        restoreAllListeners();
	
    }

    //**************************************************
    // END process_a_msg
    //**************************************************
    //**************************************************
    // initialiseWebSocketConnection
    //**************************************************

    function initialiseWebSocketConnection() {
        try {
            SOCKET = new WebSocket(webSocketAddress);
        } catch (e) {
            alert("Sorry, your browser does not support HTML5 and/or WebSockets!");
        }
        SOCKET.onopen = function () {
            console.log("WebSocket connection established!");
            var msg = {
                type: 14,
                data: {
                    name: myName
                }
            };
            console.log("sending" + JSON.stringify(msg));
            SOCKET.send(JSON.stringify(msg));
            msg = null;
            if (TEST_MODE) {
                runPreGameTests();
            }
        };
        SOCKET.onclose = function () {
            alert("WebSocket connection broken!");
        };
        SOCKET.onmessage = function (msg) {
            MSG_BUFFER.push(msg.data);
            process_a_msg();
        };
    }

    //**************************************************
    // END initialiseWebSocketConnection
    //**************************************************
    

 
    //center the canvas in the window (2.1 is a fudge factor from 2 to improve the centering):
    document.getElementById("board").style.marginLeft = parseInt((window.innerWidth - CANVAS_SIZE) / 2.1, 10) + "px"; //10 ensures decimal radix (not octal)
  
    backgroundLayer.add(background);
    stage.add(backgroundLayer);

   
    getPreGameInfo(); //initialise gameId and preConnectedPlayers
    initialisePregame();
    
    initialiseWebSocketConnection(); // WEBSOCKET CONNECTION ESTABLISHED HERE
    preGameLayer.draw();
    stage.add(preGameLayer);

   


    //**************************************************
    // Test area run after initialisation
    //**************************************************

    function runPreGameTests() {
        //THIS IS JUST FOR TESTING - REMOVE AFTER TESTING
        var incomingPlayers = ["robSE13", "NellyTheElly"];
        var msg = null;
        if (pC > 4) {
            msg = {
                type: 10,
                data: {
                    p1: "MrElephant",
                    p2: "robSE13",
                    p3: "boaty666",
                    p4: "NellyTheElly"
                }
            };
        } else {
            msg = {
                type: 9,
                data: {
                    name: incomingPlayers[pC - preConnectedPlayers.length - 1]
                }
            };
        }
        //alert("sending" + JSON.stringify(msg));
        SOCKET.send(JSON.stringify(msg));
        msg = null;

        if (pC <= 4) {
            window.setTimeout(runPreGameTests, 8000);
        }
    }

    var squareNo = 0;
    var msgNo = 1;
    var movesList = ["G4", "F4", //1
    "B1", "C3", "B6", "C6", "H7", "H6", "G3", "F3", "B2", "B3", "A5", "B6", "H8", "F6", "H4", "G4", "A2", "A3", //10
    "A6", "A3", //11
    //12 //blue elephant takes green pawn
    "F6", "D4", "F3", "E3", "D2", "E3", //15
    //16 //green pawn takes red pawn
    "C6", "D6", "G7", "G6", //"E3","D3", //illegal move - out of turn)
    "F4", "E4", "D1", "D2", //20
    "D6", "E7", //21
    //22 //blue pawn takes yellow pawn
    "H6", "H5", "H3", "H5", //24
    //25 //red elephant takes yellow pawn
    "C1", "G1", //26
    //27 //green elephant takes red pawn
    "E7", "F8", //28
    //29 //blue pawn takes yellow elephant
    "G6", "H5", //30
    //31 //yellow pawn takes red elephant
    "G4", "H5", //32
    //33 //red king takes yellow pawn
    "G1", "G2", //34
    //35 //green elephant takes red pawn
    "A8", "C6", "D4", "B6", //37 
    //38 //yellow boat takes blue king
    //39 //playerOut p3
    "E4", "D4", //40
    //41 //"G2","H1", //should fail - elephant moves diagonally
    //42 //invalid move
    "G2", "H2", //43
    //44 //green elephant takes red knight
    //"F2","F3", // should fail - no piece at from square or to square
    "F7", "F6", //45
    "D4", "C4", "C3", "D5", "F6", "F5", "H1", "F3", "A1", "C3", //50 
    "F5", "F4", //51
    "F3", "D5", //52
    //53 //red boat takes green knight 
    "C3", "A5", "F4", "F3", "C4", "B4", "A5", "C7", "F3", "F2", "B4", "A4", //red pawn is now in promotion position
    "D2", "E1", "E8", "F8", //61
    //62 //yellow king takes blue pawn
    "D5", "B7", //63
    //64 //red boat takes blue pawn
    //65  - boat triumph!!
    //66 //update score after boat triumph
    //"G2","H1", //fails - there's nothing here
    "E1", "F1", //67
    //"G8","F7", // should fail - knight moving in 1 direct diagonal
    "G8", "F6", //68
    "B7", "D5", "F1", "G1", "F6", "H5", //71
    //72 //yellow knight takes red king 
    //73 (pawn promotion?) at a4 to a king
    "A4", "A3", //74
    //75 //new red king takes blue elephant
    "H2", "H5", //76
    //77 //green elephant takes yellow knight 
    "F2", "G1", //78
    //79 //yellow pawn takes green king 
    //80 (yellow pawn promotes to knight)
    //81 //playerOut p2
    //82 //sudden death
    //"F6","H5", //error there's nothing here
    "A3", "B3", //83
    //84 //new red king takes green pawn
    "G1", "F3", //new yellow knight moves
    "B3", "C2", //86
    //87 //new red king takes green pawn
    "F8", "G8", "C2", "D2", "F3", "D2"];
    //91 //new yellow king takes new red king and wins 
    //92 //

    function runTests() {
        var msg;
        if (msgNo === 12) {
            msg = {
                type: 4,
                data: {
                    p1: 0,
                    p2: 0,
                    p3: 1,
                    p4: 0
                }
            };
        } else if (msgNo === 16) {
            msg = {
                type: 4,
                data: {
                    p1: 0,
                    p2: 1,
                    p3: 1,
                    p4: 0
                }
            };
        } else if (msgNo === 22) {
            msg = {
                type: 4,
                data: {
                    p1: 0,
                    p2: 1,
                    p3: 2,
                    p4: 0
                }
            };
        } else if (msgNo === 25) {
            msg = {
                type: 4,
                data: {
                    p1: 1,
                    p2: 1,
                    p3: 2,
                    p4: 0
                }
            };
        } else if (msgNo === 27) {
            msg = {
                type: 4,
                data: {
                    p1: 1,
                    p2: 2,
                    p3: 2,
                    p4: 0
                }
            };
        } else if (msgNo === 29) {
            msg = {
                type: 4,
                data: {
                    p1: 1,
                    p2: 2,
                    p3: 6,
                    p4: 0
                }
            };
        } else if (msgNo === 31) {
            msg = {
                type: 4,
                data: {
                    p1: 1,
                    p2: 2,
                    p3: 6,
                    p4: 4
                }
            };
        } else if (msgNo === 33) {
            msg = {
                type: 4,
                data: {
                    p1: 2,
                    p2: 2,
                    p3: 6,
                    p4: 4
                }
            };
        } else if (msgNo === 35) {
            msg = {
                type: 4,
                data: {
                    p1: 2,
                    p2: 3,
                    p3: 6,
                    p4: 4
                }
            };
        } else if (msgNo === 38) {
            msg = {
                type: 4,
                data: {
                    p1: 2,
                    p2: 3,
                    p3: 6,
                    p4: 9
                }
            };
        } else if (msgNo === 39) {
            msg = {
                type: 5,
                data: {
                    player: 3
                }
            };
        } else if (msgNo === 41) {
            //alert("Try to move a blue piece" + "\nthen try to move a green piece to another green piece square" + "\nthen move from G2 to H1");
        } else if (msgNo === 42) {
            //msg = {
            //    type: 3,
            //    data: null
            //};
        } else if (msgNo === 44) {
            msg = {
                type: 4,
                data: {
                    p1: 2,
                    p2: 6,
                    p3: 6,
                    p4: 9
                }
            };
        } else if (msgNo === 53) {
            msg = {
                type: 4,
                data: {
                    p1: 5,
                    p2: 6,
                    p3: 6,
                    p4: 9
                }
            };
        } else if (msgNo === 62) {
            msg = {
                type: 4,
                data: {
                    p1: 5,
                    p2: 6,
                    p3: 6,
                    p4: 10
                }
            };
        } else if (msgNo === 64) {
            msg = {
                type: 4,
                data: {
                    p1: 6,
                    p2: 6,
                    p3: 6,
                    p4: 10
                }
            };
        } else if (msgNo === 65) {
            msg = {
                type: 11,
                data: {
                    tBoat: "B7",
                    boats: ["B6", "C6", "C7"]
                }
            };
        } else if (msgNo === 66) {
            msg = {
                type: 4,
                data: {
                    p1: 12,
                    p2: 6,
                    p3: 6,
                    p4: 10
                }
            };
        } else if (msgNo === 72) {
            msg = {
                type: 4,
                data: {
                    p1: 12,
                    p2: 6,
                    p3: 6,
                    p4: 15
                }
            };
        } else if (msgNo === 73) {
            msg = {
                type: 8,
                data: {
                    sq: "A4",
                    pieceType: "king"
                }
            };
        } else if (msgNo === 75) {
            msg = {
                type: 4,
                data: {
                    p1: 16,
                    p2: 6,
                    p3: 6,
                    p4: 15
                }
            };
        } else if (msgNo === 77) {
            msg = {
                type: 4,
                data: {
                    p1: 16,
                    p2: 9,
                    p3: 6,
                    p4: 15
                }
            };
        } else if (msgNo === 79) {
            msg = {
                type: 4,
                data: {
                    p1: 16,
                    p2: 9,
                    p3: 6,
                    p4: 69
                }
            };
        } else if (msgNo === 80) {
            msg = {
                type: 8,
                data: {
                    sq: "G1",
                    pieceType: "knight"
                }
            };
        } else if (msgNo === 81) {
            msg = {
                type: 5,
                data: {
                    player: 2
                }
            };
        } else if (msgNo === 82) {
            msg = {
                type: 7,
                data: {
                    moveLimit: 10
                }
            };
        } else if (msgNo === 84) {
            msg = {
                type: 4,
                data: {
                    p1: 17,
                    p2: 9,
                    p3: 6,
                    p4: 69
                }
            };
        } else if (msgNo === 87) {
            msg = {
                type: 4,
                data: {
                    p1: 18,
                    p2: 9,
                    p3: 6,
                    p4: 69
                }
            };
        } else if (msgNo === 91) {
            msg = {
                type: 4,
                data: {
                    p1: 18,
                    p2: 9,
                    p3: 6,
                    p4: 74
                }
            };
        } else if (msgNo === 92) {
            msg = {
                type: 6,
                data: {
                    outcome: "win",
                    player: [4]
                }
            };
        } else {
            msg = {
                type: 2,
                data: {
                    from_sq: movesList[squareNo++],
                    to_sq: movesList[squareNo++]
                }
            };
        }
        if (msgNo !== 41) {
            // alert("sending" + JSON.stringify(msg));
            SOCKET.send(JSON.stringify(msg));

        }

        msgNo++;
        if (msgNo < 93) {
            if (!msg) {
                window.setTimeout(runTests, 3000);
            } else if (msg.type === 2) {
                window.setTimeout(runTests, 3000);
            } else {
                window.setTimeout(runTests, 3000);
            }
        } else {
            //alert("End of tests!!");
        }
        msg = null;
    }

    //**************************************************
    // End Test area 
    //**************************************************
} //initStage closing brace
loadImages(image_sources, initStage);
