package KnightsSword;

import java.awt.Rectangle;

import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.input.mouse.destination.impl.shape.RectangleDestination;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.wrappers.interactive.GameObject;

@ScriptManifest(category = Category.QUEST, name = "knightsSword", author = "Himouto", version = 2.03)
public class Quester extends AbstractScript {

	private final int knightsSwordProgress = 122;

	private final Area castleGrounds = new Area(2951, 3326, 3004, 3350);
	private final Area vLibrary = new Area(3207, 3487, 3217, 3497);
	private final Area thurgoArea = new Area(2995, 3140, 3002, 3149);
	private final Area vyvinsRoom = new Area(2981, 3334, 2986, 3343, 2);
	private final Area secondFloor = new Area(2981, 3340, 2985, 3343, 1);
	private final Area caveEntrance = new Area(3007, 3148, 3010, 3151);
	private final Area wholeCave = new Area(2991, 9545, 3067, 9587);
	private final Area caveExit = new Area(3007, 9548, 3009, 9552);
	private final Area bluriteMine = new Area(3048, 9567, 3051, 9569);
	private final Area faladorIntermediary = new Area(3001, 3352, 3008, 3354);
	private final Area safeSpot = new Area(3036, 9578, 3037, 9580);
	private final Area runBackIntermediary = new Area(3006, 9578, 3010, 9580);
	
	private final Tile bluriteSpot = new Tile(3049, 9567);

	private final String Ice_Warrior = "Ice Warrior";
	private final String Thurgo = "Thurgo";
	private final String Squire = "Squire";
	private final String Reldo = "Reldo";

	private boolean wait = true;
	private boolean doOnce = true;
	private boolean dontMove = false;
	private boolean faladorIntermediaryReached = false;
	private boolean runBackIntermediaryReached = false;

	@Override
	public int onLoop() {
		int conf = getPlayerSettings().getConfig(knightsSwordProgress);
		switch (conf) {

		//starting quest
		case 0:
			if (castleGrounds.contains(getLocalPlayer())) {
				sleepUntil(() -> talkToSquire(), 50000);
			} else {
				getWalking().walk(castleGrounds.getCenter());
			}
			break;

		//talking to Reldo
		case 1:
			if (vLibrary.contains(getLocalPlayer())) {
				sleepUntil(() -> talkToReldo(), 50000);
			} else {
				getWalking().walk(vLibrary.getCenter());
			}
			break;
		
		//talking to the snappy Thurgo, and feed him pie
		case 2:
			if (thurgoArea.contains(getLocalPlayer())) {
				sleepUntil(() -> talkToThurgo(), 50000);
			} else {
				getWalking().walk(thurgoArea.getCenter());
			}
			break;

		//talk to Thurgo after he is fed
		case 3:
			if (thurgoArea.contains(getLocalPlayer())) {
				sleepUntil(() -> talkToThurgo(), 50000);
				faladorIntermediaryReached = false;
			} else {
				getWalking().walk(thurgoArea.getCenter());
			}
			break;

		//go back to squire to ask for a picture of the sword
		case 4:
			//sometimes when walking from south of falador, the walker will not walk you into the castle, but with the intermediary, I have bypassed it
			if (faladorIntermediaryReached) {
				if (castleGrounds.contains(getLocalPlayer())) {
					sleepUntil(() -> talkToSquire(), 50000);
					doOnce = true;
				} else {
					getWalking().walk(castleGrounds.getCenter());
				}
			} else {
				if (faladorIntermediary.contains(getLocalPlayer())) {
					faladorIntermediaryReached = true;
				}
				getWalking().walk(faladorIntermediary.getCenter());
			}
			break;

		//search the cupboard for the pie
		case 5:
			//go back to thurgo when you have the portrait
			if (getInventory().contains("Portrait")) {
				if (thurgoArea.contains(getLocalPlayer())) {
					sleepUntil(() -> talkToThurgo(), 30000);
				} else {
					getWalking().walk(thurgoArea.getCenter());
				}
			} else if (secondFloor.contains(getLocalPlayer())) {
				GameObject stairCase = getGameObjects().closest(
						staircase -> staircase != null
								&& staircase.getName().equals("Staircase"));
				if (sleepUntil(() -> stairCase.interact("Climb-up"), 5000)) {
					sleepUntil(() -> !getLocalPlayer().isMoving(), 5000);
				}
			} else if (vyvinsRoom.contains(getLocalPlayer())) {
				GameObject door = getGameObjects().closest(
						door_ -> door_ != null
								&& door_.getName().equals("Door"));
				if (door.hasAction("Open")) {
					sleepUntil(() -> door.interact("Open"), 5000);
				} else {
					NPC sir_Vyvin = getNpcs().closest(
							npc -> npc != null
									&& npc.getName().equals("Sir Vyvin"));
					if (sir_Vyvin.getLocalY() < 3337) {
						GameObject cupBoard = getGameObjects().closest(
								cupboard -> cupboard != null
										&& cupboard.getName()
												.equals("Cupboard"));
						if (cupBoard.hasAction("Open")) {
							sleepUntil(() -> cupBoard.interact("Open"), 5000);
						}
						if (cupBoard.hasAction("Search")) {
							if (sleepUntil(() -> cupBoard.interact("Search"),
									5000)) {
								if (sleepUntil(() -> cupBoardDialouge(), 10000)) {
									GameObject stairCaseDown = getGameObjects()
											.closest(
													staircase -> staircase != null
															&& staircase
																	.getName()
																	.equals("Staircase"));
									sleepUntil(
											() -> stairCaseDown
													.interact("Climb-down"),
											5000);
								}
							}
						}
					}

				}
			} else {
				if(doOnce) {
					GameObject staircase = getGameObjects().closest(stair -> stair.getName().equals("Staircase"));
					if(staircase != null) {
						if(staircase.interact("Climb-up")) {
							sleepUntil(() -> !getLocalPlayer().isMoving(), 8000);
							doOnce = false;
						}
					}
				} else {
					getWalking().walk(secondFloor.getCenter());
				}
			}
			break;

		//mine the blurite ore, give it to Thurgo to make the sword, return it to the squire
		case 6:
			if (getInventory().contains("Blurite sword")) {
					if (faladorIntermediaryReached) {
						if (castleGrounds.contains(getLocalPlayer())) {
							sleepUntil(() -> talkToSquire(), 50000);
						} else {
							getWalking().walk(castleGrounds.getCenter());
						}
					} else {
						if (faladorIntermediary.contains(getLocalPlayer())) {
							faladorIntermediaryReached = true;
						}
						getWalking().walk(faladorIntermediary.getCenter());
					}
				} else if (getInventory().contains("Blurite ore")) {
						if (wholeCave.contains(getLocalPlayer())) {
							if (caveExit.contains(getLocalPlayer())) {
								GameObject ladder = getGameObjects().closest(
										ladder_ -> ladder_ != null
												&& ladder_.hasAction("Climb-up"));
								if (ladder.interact("Climb-up")) {
									sleep(Calculations.random(1000, 3000));
								}
							} else {
								if(runBackIntermediaryReached) {
									getWalking().walk(caveExit.getCenter());
								} else {
									if(runBackIntermediary.contains(getLocalPlayer())) {
										runBackIntermediaryReached = true;
									} else {
										getWalking().walk(runBackIntermediary.getCenter());
									}
								}
								
							}
						} else {
							if (thurgoArea.contains(getLocalPlayer())) {
								sleepUntil(() -> talkToThurgo(), 50000);
							} else {
								getWalking().walk(thurgoArea.getCenter());
							}
						}
					} else {
						if(wholeCave.contains(getLocalPlayer())) {
							if (bluriteMine.contains(getLocalPlayer())) {
								if (dontMove) {
									GameObject blurite = getGameObjects().closest(
											blurite_ -> blurite_ != null
													&& blurite_.hasAction("Mine"));
									if (blurite.interact("Mine")) {
										sleepUntil(() -> getLocalPlayer()
												.getAnimation() == -1, 60000);
									}
								} else {
									sleepUntil(
											() -> getMouse().click(new RectangleDestination(getClient(), new Rectangle((int) getMap().getBounds(bluriteSpot).getCenterX()-1, (int) getMap().getBounds(bluriteSpot).getCenterY()-1, 2, 2))),
											10000);
									dontMove = true;
								}
							} else {
								if(wait) {
									if(safeSpot.contains(getLocalPlayer())) {
										NPC iceWarrior = getNpcs().closest(Ice_Warrior);
										if(iceWarrior.getY() > 9577) {
											wait = false;
										}
									} else {
										getWalking().walk(safeSpot.getCenter());
										}
									} else {
										getWalking().walk(bluriteMine.getCenter());
									}
							}
					} else {
						if (caveEntrance.contains(getLocalPlayer())) {
								GameObject trapDoor = getGameObjects()
										.closest(
												trapdoor -> trapdoor != null
												&& trapdoor
													.hasAction("Climb-down"));
								if (trapDoor.interact("Climb-down")) {
									wait = true;
									sleep(Calculations.random(500, 600));
								}
						} else {
							getWalking().walk(caveEntrance.getCenter());
						}
					}
				}
			break;

		case 7:
			return -1;
		}
		return Calculations.random(200, 300);
	}

	public boolean talkToSquire() {

		final NPC guide = getNpcs().closest(Squire);
		if (getDialogues().inDialogue() == false) {
			if (guide != null) {
				if (guide.isOnScreen()) {
					if (guide.interact("Talk-to")) {
						sleepUntil(new Condition() {
							public boolean verify() {
								return getDialogues().canContinue();
							}
						}, Calculations.random(1200, 1600));
					}
				} else {
					getWalking().walk(guide);
				}
			}
		}
		if (!getDialogues().canContinue()) {
			getDialogues().chooseOption(1);
		} else {
			getDialogues().clickContinue();
			sleep(600, 900);
			if (!getDialogues().inDialogue()) {
				return true;
			}
		}
		return false;
	}

	public boolean talkToReldo() {

		final NPC guide = getNpcs().closest(Reldo);
		if (getDialogues().inDialogue() == false) {
			if (guide != null) {
				if (guide.isOnScreen()) {
					if (guide.interact("Talk-to")) {
						sleepUntil(new Condition() {
							public boolean verify() {
								return getDialogues().canContinue();
							}
						}, Calculations.random(1200, 1600));
					}
				} else {
					getWalking().walk(guide);
				}
			}
		}
		if (!getDialogues().canContinue()) {
			getDialogues().chooseOption(4);
		} else {
			getDialogues().clickContinue();
			sleep(600, 900);
			if (!getDialogues().inDialogue()) {
				return true;
			}
		}
		return false;
	}

	public boolean talkToThurgo() {

		final NPC guide = getNpcs().closest(Thurgo);
		if (getDialogues().inDialogue() == false) {
			if (guide != null) {
				if (guide.isOnScreen()) {
					if (guide.interact("Talk-to")) {
						sleepUntil(new Condition() {
							public boolean verify() {
								return getDialogues().canContinue();
							}
						}, Calculations.random(1200, 1600));
					}
				} else {
					getWalking().walk(guide);
				}
			}
		}
		if (!getDialogues().canContinue()) {
			getDialogues().chooseOption(2);
		} else {
			getDialogues().clickContinue();
			sleep(600, 900);
			if (!getDialogues().inDialogue()) {
				return true;
			}
		}
		return false;
	}

	public boolean cupBoardDialouge() {

		if (!getDialogues().canContinue()) {
		} else {
			getDialogues().clickContinue();
			sleep(600, 900);
			if (getDialogues().inDialogue()) {
				return true;
			}
		}
		return false;
	}
}
