package jeu.modele;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class Plateau {
	
	private int n;
	private int max;
	private List<Case> cases;
	
	/**
	 * Générer un plateau aléatoire
	 * @param n la taille du plateau
	 * @param max la valeur maximale d'une cellule
	 */
	public Plateau RemplirGrilleAleatoire(int n, int max) {
		this.n = n;
		this.max = max;
		cases = new ArrayList<>(n * n);
		
		Random ran = new Random();
		for (int y = 0; y < n; y++) {
			for(int x = 0; x < n; x++) {
				cases.add(new Case(x, y, ran.nextInt(max) + 1));
			}
		}
		
		return this;
	}
	
	/**
	 * Génère un plateau à partir d'un fichier
	 * @param file le nom du fichier à charger
	 */
	public Plateau RemplirGrilleFichier(String nomFichier, Joueur[] joueurs) {
		File fichier = new File(nomFichier);
		try {
			Scanner scan = new Scanner(fichier);
			
			n = Integer.parseInt(scan.next());
			max = Integer.parseInt(scan.next());
			cases = new ArrayList<>(n * n);
			
			// remplissage du plateau
			for (int y = 0; y < n; y++) {
				for(int x = 0; x < n; x++) {
					int valeur = Integer.parseInt(scan.next());
					cases.add(new Case(x, y, Math.min(valeur, max)));
				}
			}
			
			// attribution des cases aux joueurs
			for (int y = 0; y < n; y++) {
				for(int x = 0; x < n; x++) {
					int id = Integer.parseInt(scan.next());
					if (id == 0) continue;
					Joueur proprietaire = joueurs[id - 1];
					ColorerCase(x, y, proprietaire);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	/**
	 * Change la couleur de la case en fonction de la couleur d'un des joueurs de la partie
	 * @param x la position horizontale de la case à colorier
	 * @param y la position verticale de la case à colorier
	 * @param proprietaire le joueur ayant pris la case
	 */
	public void ColorerCase(int x, int y, Joueur proprietaire) {
		Case nouvelleCase = getCase(x, y);
		nouvelleCase.setProprietaire(proprietaire);
		
		// relier les nouveaux groupes créés entre eux
		RelierComposantes(x, y, proprietaire, true);
		
		// rattacher la nouvelle case à la nouvelle composante composée
		List<Case> adjacentes = getCasesAdjacentes(nouvelleCase, proprietaire);
		if (adjacentes.size() == 0) {
			return; // si aucune case de même couleur adjacente
		}
		nouvelleCase.union(adjacentes.get(0));
	}
	
	public int getTaille() {
		return n;
	}

	public List<Case> getCases() {
		return cases;
	}
	
	public Case getCase(int x, int y) {
		if (x < 0 || y < 0 || x >= n || y >= n) return null;
		return cases.get(y * n + x);
	}
	
	public Case getCase(int i) {
		if (i < 0 || i > n * n) return null;
		return cases.get(i);
	}
	
	/**
	 * Teste si il existe un chemin d'une couleur spécifique entre deux cases
	 * @param c1 la première case à tester
	 * @param c2 la seconde case à tester
	 * @param proprietaire le joueur de la couleur dont on souhaite tester le chemin
	 * @return 
	 * 	un tableau contenant les deux représentants de c1 et c2 si ils sont différents (pas de chemin).
	 * 	retourne null si les représentants sont égaux (un chemin existe)
	 */
	public Case[] ExisteCheminCases(Case c1, Case c2, Joueur proprietaire) {
		Case representant1 = c1.classe();
		Case representant2 = c2.classe();
		
		if (c1.getProprietaire() != c2.getProprietaire() && c1.getProprietaire() != proprietaire) 
			return new Case[] {representant1, representant2};
		
		return (representant1 != representant2) ? new Case[] {representant1, representant2} : null;
	}
	
	/**
	 * Teste si la coloration d'une certaine case de la couleur d'un joueur permet de relier deux composantes déjà présentes sur le plateau
	 * @param x la position horizontale de la case à tester
	 * @param y la position verticale de la case à tester
	 * @param joueur le joueur de la couleur dont on souhaite tester la liaison
	 * @param lier si true, la liaison sera automatiquement effectuée, sinon, le test de liaison sera retourné sans création de nouvelles liaisons
	 * 
	 * @return true si la coloration d'une case permet de relier plusieurs composantes 
	 */
	public boolean RelierComposantes(int x, int y, Joueur joueur, boolean lier) {
		Case nouvelleCase = getCase(x, y);
		List<Case> adjacentes = getCasesAdjacentes(nouvelleCase, joueur);
		boolean touteLiees = true;
		// complexité de ces deux boucles constante car quelque soit N, la liste des adjacents vérifiera toujours |adjacents| <= 4
		for (Case c1 : adjacentes) {
			for (Case c2 : adjacentes) {
				if (c1 == c2) continue;
				
				Case[] representants = ExisteCheminCases(c1, c2, joueur);
				boolean liees = (representants == null);
				if (lier && !liees) {
					representants[0].union(representants[1]);
				}
				touteLiees = touteLiees && liees;
				// @TODO : possibilité d'optimiser un peu en quittant la boucle si toutLiees = false et !lier
			}
		}
		return !touteLiees;
	}
	
	/**
	 * Permet de récupérer les cases adjacentes à une case du plateau
	 * @param c la case à tester
	 * @param joueur, si précisé, ne retourne que les cases adjacentes appartenant à ce joueur
	 * @return la liste des cases adjacentes à c
	 */
	public List<Case> getCasesAdjacentes(Case c, Joueur joueur) {
		List<Case> cases = new ArrayList<>(4);
		int x = c.getX(),
			y = c.getY();
		
		Case gauche = getCase(x - 1, y);
		if (gauche != null && gauche.getProprietaire() == joueur) cases.add(gauche);

		Case droite = getCase(x + 1, y);
		if (droite != null && droite.getProprietaire() == joueur) cases.add(droite);
		
		Case haut = getCase(x, y - 1);
		if (haut != null && haut.getProprietaire() == joueur) cases.add(haut);
		
		Case bas = getCase(x, y + 1);
		if (bas != null && bas.getProprietaire() == joueur) cases.add(bas);
			
		return cases;
	}
	
	public List<Case> getCasesAdjacentes(Case c) {
		return getCasesAdjacentes(c, null);
	}
}
