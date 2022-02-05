package com.cursoandroid.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class jogo extends ApplicationAdapter {

	//Texturas
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gamerOver;

	//Formas para colisão
	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;

	//Atributos de configurações
	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao = 0;
	private float gravidade = 2;
	private float posicaoInicialVerticalPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoEntreCanos;
	private Random random;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;
	private boolean passouCano;
	private int estadojogo = 0;
	private float posicaoHorizontalPassaro = 0;

	//Exibição de textos
	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;

	//Configuração de sons
	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;

	//Objeto salvar pontuacao
	Preferences preferencias;

	//Objetos para câmera
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;

	@Override
	public void create () {

		inicializarTexturas();
		inicializarObjetos();
	}

	@Override
	public void render () {

		//limpar frames anteriores
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		verificarEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();
	}

	private void verificarEstadoJogo(){

		boolean toqueTela = Gdx.input.justTouched();

		if (estadojogo == 0){
			//Aplica evento de clique na tela
			if (toqueTela){
				gravidade = -15;
				estadojogo = 1;
				somVoando.play();
			}

		}else if (estadojogo == 1){
			//Aplica evento de clique na tela
			if (toqueTela){
				gravidade = -15;
				estadojogo = 1;
				somVoando.play();
			}

			//Movimentar o cano
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			if (posicaoCanoHorizontal < -canoTopo.getWidth()){
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(800) -400;
				passouCano = false;
			}

			//Aplica gravidade no passaro
			if (posicaoInicialVerticalPassaro > 0 || gravidade < 0)
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;

			gravidade++;

		}else if (estadojogo == 2){

			//Aplica gravidade no passaro
			/*if (posicaoInicialVerticalPassaro > 0 || gravidade < 0)
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
				gravidade++;*/
			if (pontos > pontuacaoMaxima){
				pontuacaoMaxima = pontos;
				preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
			}

			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime()*500;

			//Aplica evento de clique na tela
			if (toqueTela){
				estadojogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalPassaro = 0;
				posicaoInicialVerticalPassaro = alturaDispositivo / 2;
				posicaoCanoHorizontal = larguraDispositivo;
			}
		}

	}

	private void detectarColisoes(){

		circuloPassaro.set(50 + posicaoHorizontalPassaro + passaros[0].getWidth()/2, posicaoInicialVerticalPassaro + passaros[0].getHeight()/2, passaros[0].getWidth()/2);
		retanguloCanoBaixo.set(
				posicaoCanoHorizontal, alturaDispositivo/2 -canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight()
		);
		retanguloCanoCima.set(
				posicaoCanoHorizontal, alturaDispositivo/2 + espacoEntreCanos /2 + posicaoCanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight()
		);

		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);

		if (colidiuCanoCima || colidiuCanoBaixo){
			Gdx.app.log("Log", "Colidiu");

			if (estadojogo == 1){
				somColisao.play();
				estadojogo = 2;
			}
		}

	}

	private void desenharTexturas(){

		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
		batch.draw(passaros[(int) variacao], 50 + posicaoHorizontalPassaro, posicaoInicialVerticalPassaro);
		batch.draw(canoBaixo, posicaoCanoHorizontal, alturaDispositivo /2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoVertical);
		batch.draw(canoTopo, posicaoCanoHorizontal, alturaDispositivo /2 + espacoEntreCanos /2 + posicaoCanoVertical);
		textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo/2, alturaDispositivo -100);

		if (estadojogo == 2){
			batch.draw(gamerOver, larguraDispositivo /2 - gamerOver.getWidth()/2,alturaDispositivo/2);
			textoReiniciar.draw(batch, "Toque para reiniciar", larguraDispositivo/2 -140, alturaDispositivo/2 - gamerOver.getHeight()/2);
			textoMelhorPontuacao.draw(batch, "Seu record é: " + pontuacaoMaxima + "pontos", larguraDispositivo/2 -140,alturaDispositivo/2 - gamerOver.getHeight());
		}

		batch.end();
	}

	public void validarPontos(){
		if (posicaoCanoHorizontal < 50-passaros[0].getWidth()){
			if (!passouCano){
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}

		variacao+= Gdx.graphics.getDeltaTime() * 10;
		//Verefica varição para bater asas do passáro
		if (variacao > 3)
			variacao = 0;
	}

	private void inicializarTexturas(){

		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");

		fundo = new Texture("fundo.png");
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gamerOver = new Texture("game_over.png");
	}

	private void inicializarObjetos(){
		batch = new SpriteBatch();
		random = new Random();

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDispositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 350;

		//Configuracao dos textos
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);

		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(2);

		//Formas Geométicas para colisoes;
		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();

		//Inicializa Sons
		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		//Configura preferencias dos objetos
		preferencias = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima", 0);

		//Configuração da câmera
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	@Override
	public void dispose () {

	}
}
