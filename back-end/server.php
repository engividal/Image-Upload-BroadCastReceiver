<?php 

	if (file_get_contents('php://input')){
		
		$json = json_decode(file_get_contents('php://input'));
		//capturando o json da imagem
		$imagemRecebida = $json->{'imagem'};
		//nome da imagem
    	$imgNome = 'img-'.time().'.jpeg';
    	 // gravar a imagem na pasta 'img'
    	$Imagembinary = base64_decode($imagemRecebida);
		$file = fopen('img/'.$imgNome, 'wb');
		fwrite($file, $Imagembinary);
		fclose($file);
	}

 ?>