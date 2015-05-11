# Bubble Docs SD-STORE
# Projecto de Sistemas DistribuÃ­dos #

## Primeira entrega ##

Grupo de SD 67

David Silva 76511 david.p.silva93@gmail.com
Vanessa Gaspar 73995 vanessa.gaspar@tecnico.ulisboa.pt
Marco Tomas 65921 m.ft.tomas@gmail.com

RepositÃ³rio:
[tecnico-softeng-distsys-2015/T_03_59_67-project](https://github.com/tecnico-softeng-distsys-2015/T_03_59_67-project/)


-------------------------------------------------------------------------------

## ServiÃ§o SD-STORE 

### InstruÃ§Ãµes de instalaÃ§Ã£o 
*(Como colocar o projecto a funcionar numa mÃ¡quina do laboratÃ³rio)*

[0] Iniciar sistema operativo

Windows

[1] Iniciar servidores de apoio

JUDDI:
> startup .bat

[2] Criar pasta temporÃ¡ria

> cd  C:\
> mkdir grupo67
> cd grupo67

[3] Obter versÃ£o entregue

> git clone -b SD-STORE_R_2 https://github.com/tecnico-softeng-distsys-2015/T_03_59_67-project/


[4] Construir e executar **servidor**

> cd T_03_59_67-project/sd-store
> mvn clean package 
> mvn exec:java


[5] Construir **cliente**

> cd ..
> cd sd-store-cli
> mvn clean
> mvn generate-sources

...


-------------------------------------------------------------------------------

### InstruÃ§Ãµes de teste: ###
*(Como verificar que todas as funcionalidades estÃ£o a funcionar correctamente)*


[1] Executar **cliente de testes** ...

> cd T_03_59_67-project/sd-store
> mvn test

-------------------------------------------------------------------------------
**FIM**