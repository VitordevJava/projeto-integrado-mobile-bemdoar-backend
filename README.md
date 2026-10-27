# BemDoar - Backend

## Sistema de gestão organizacional social 
O BemDoar é um sistema de gestão dedicado a uma única organização social, desenvolvido em parceria real com o clube do idoso Nina Cruz (Gama-DF)
O objetivo é organizar, em um unico sistema, as informaçoes, necessidades, doações sociais da instituiçoes 

Java 21 + Spring Boot 3.5 + Spring Security (JWT) + JPA + H2.

## Rodar

    ./mvnw spring-boot:run        (Mac/Linux)
    mvnw.cmd spring-boot:run      (Windows)

Sobe em http://localhost:8080

Nao precisa instalar Maven nem banco de dados. O `mvnw` baixa o Maven
sozinho na primeira vez e o H2 cria o arquivo `./data/bemdoar.mv.db`.

## Contas criadas automaticamente

| Perfil        | E-mail             | Senha    |
|---------------|--------------------|----------|
| ADMINISTRADOR | admin@bemdoar.com  | admin123 |
| USUARIO       | maria@email.com    | maria123 |

## Conferir que subiu

    http://localhost:8080/api/necessidades   -> deve devolver JSON
    http://localhost:8080/h2-console         -> console do banco
       JDBC URL: jdbc:h2:file:./data/bemdoar
       User: sa      Password: (vazio)

## Camadas

    controller  -> recebe HTTP, define permissao, chama o service
    service     -> REGRAS DE NEGOCIO (todo if de "pode/nao pode" mora aqui)
    repository  -> acesso ao banco (so assinatura de metodo, sem SQL)
    entity      -> tabela do banco
    dto         -> formato do JSON que entra e que sai

## CRUD-molde

`CategoriaNecessidade` e o modelo a ser copiado. Percorra nesta ordem:

    entity/CategoriaNecessidade.java
    repository/CategoriaNecessidadeRepository.java
    dto/CategoriaRequest.java
    dto/CategoriaResponse.java
    service/CategoriaNecessidadeService.java
    controller/CategoriaController.java

## Arquivos que NINGUEM deve editar

    config/SecurityConfig.java
    config/WebConfig.java
    config/DataSeeder.java
    security/*
    exception/*
    service/UsuarioService.java
    controller/AuthController.java

<!-- Submissão do Backend -->
