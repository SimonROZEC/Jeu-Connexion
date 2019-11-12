package jeu.controleur;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import jeu.modele.Case;
import jeu.modele.Joueur;
import jeu.modele.Ordinateur;
import jeu.modele.Partie;
import jeu.vue.BoutonCase;
import jeu.vue.VueJeu;

/**
 * Contôleur s'éxécutant lors de la sélection d'une case par un joueur
 */
public class ControleurSelectionCase implements ActionListener {

	private Partie modele;
	private VueJeu vue;

	public ControleurSelectionCase(Partie modele, VueJeu vue) {
		this.modele = modele;
		this.vue = vue;
	}

	public void actionPerformed(ActionEvent ev) {
		BoutonCase selection = (BoutonCase) ev.getSource();
		Case caseCliquee = modele.getPlateau().getCase(selection.getCaseX(), selection.getCaseY());
		
		/**
		 * Clic sur une case déjà occupée -> on met en surbrillance le groupe sur lequel
		 * le joueur a cliqué et on affiche le score du groupe
		 */
		if (caseCliquee.getProprietaire() != null) {
			vue.getOverlay().setCasesSurbrillances(
					modele.getPlateau().AfficherComposante(caseCliquee.getX(), caseCliquee.getY()));

			JOptionPane.showMessageDialog(null,
					"Le score de la composante sélectionnée est de "
							+ modele.getPlateau().AfficherScore(caseCliquee.getX(), caseCliquee.getY()),
					"Information Score", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		jouerTour(caseCliquee);
	}
	
	/**
	 * Joue le tour en sélectionnant une certaine case. Si un bot joue le tour, caseCliquee doit être égale à null
	 * @param caseCliquee
	 */
	private void jouerTour(Case caseCliquee) {
		/**
		 * Clic sur une case non occupée
		 */
		Joueur joueurTour = modele.getJoueurTour();
		
		if (caseCliquee == null) ((Ordinateur) joueurTour).jouer(modele);
		else joueurTour.jouer(modele, caseCliquee);
		

		// ------------ debug all cells ------------
		System.out.println("______________________________________________________");
		for (Case c : modele.getPlateau().getCases()) {
			System.out.println(c + " ##### parent: " + c.getParent() + " ##### enfants: " + c.getEnfants());
		}
		System.out.println("_ _ _ _ _ ");
		System.out.println(
				"composante : " + modele.getPlateau().AfficherComposante(caseCliquee.getX(), caseCliquee.getY()));
		System.out.println("score : " + modele.getPlateau().AfficherScore(caseCliquee.getX(), caseCliquee.getY()));
		// -----------------------------------------

		vue.getOverlay()
				.setCasesSurbrillances(modele.getPlateau().AfficherComposante(caseCliquee.getX(), caseCliquee.getY()));

		vue.getOverlay().setDernierCoup(caseCliquee);

		int[] points = modele.compterPoints();
		vue.getInformations().mettreAJourScores(points);
		
		/**
		 * Le dernier coup vient d'être joué
		 */
		if (modele.terminee()) {
			// id du joueur qui vient de gagner la partie, si égalité, l'id est négatif
			int idgagnant = (points[0] > points[1]) ? 0 : (points[1] > points[0]) ? 1 : -1;

			String affichageScores = modele.getJoueurs()[0].getNom() + " : " + points[0] + " points\n"
					+ modele.getJoueurs()[1].getNom() + " : " + points[1] + " points";
			
			if (idgagnant < 0) {
				vue.getInformations().mettreAJourVainqueur(modele.getJoueurs()[0]);
				vue.getInformations().mettreAJourVainqueur(modele.getJoueurs()[1]);
				
				JOptionPane.showMessageDialog(null,
						"Partie terminée à égalité ! \n\n" + affichageScores,
						"Partie terminée", JOptionPane.INFORMATION_MESSAGE);
			} else {
				Joueur gagnant = modele.getJoueurs()[idgagnant];
				vue.getInformations().mettreAJourVainqueur(gagnant);

				JOptionPane.showMessageDialog(null, gagnant.getNom() + " gagne la partie ! \n\n" + affichageScores,
						"Partie terminée", JOptionPane.INFORMATION_MESSAGE);
			}

		} else {
			modele.addTour();
			joueurTour = modele.getJoueurTour();
			if (joueurTour.isOrdinateur()) {
				jouerTour(null);
			}
		}

		/**
		 * Mettre à jour le compteur de tours et l'affichage du joueur qui doit jouer le
		 * prochain coup
		 */
		vue.getInformations().mettreAJourCompteur(modele.getTour(), modele.getMaxTour(), modele.getJoueurTour());
	}
}
