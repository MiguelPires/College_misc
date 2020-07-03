# Projecto de Sistemas Distribuidos #

## Primeira entrega ##

Grupo de SD 59

Miguel Pires 76433 miguel.pires@tecnico.ulisboa.pt
Joana Condeço 68624 joana.condeco@tecnico.ulisboa.pt
Ana Salta 74254 anasalta@tecnico.ulisboa.pt

Repositorio:
[tecnico-softeng-distsys-2015/T_03_59_67-project](https://github.com/tecnico-softeng-distsys-2015/T_03_59_67-project)


-------------------------------------------------------------------------------

## Servico SD-ID

### Instrucoes de instalacao 

[0] Iniciar sistema operativo

Indicar Windows ou Linux
Windows


[1] Iniciar servidores de apoio

JUDDI:
> startup.bat

[2] Criar pasta temporaria

> mkdir SD-ID-59
> cd SD-ID-59

[3] Obter versao entregue

> git clone -b SD-ID_R_1 https://github.com/tecnico-softeng-distsys-2015/T_03_59_67-project/


[4] Construir e executar **servidor**

> cd sd-id
> mvn clean -Dmaven.test.skip=true package 
> mvn exec:java


-------------------------------------------------------------------------------

### Instrucoes de teste: ###
*(Como verificar que todas as funcionalidades estao a funcionar correctamente)*


[1] Executar **cliente de testes** ...

> cd sd-id
> mvn test

-------------------------------------------------------------------------------
**FIM**