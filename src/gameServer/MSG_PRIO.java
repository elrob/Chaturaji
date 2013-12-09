/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gameServer;

/**
 * level 6: ValidMove
 * level 5: BoatTriumph
 * level 4: PawnPromotion
 * level 3: UpdateScore
 * level 2: PlayerOut
 * level 1: SuddenDeath
 * level 0: EndGame
 * 
 * @author julia
 */
public enum MSG_PRIO {
    
    EndGame, SuddenDeath, PlayerOut, UpdateScore, PawnPromotion, BoatTriumph, ValidMove
}
