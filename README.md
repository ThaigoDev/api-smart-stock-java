# Smart Stock — Sistema Inteligente de Gestão de Estoque

## 1. Introdução

O **Smart Stock** é um sistema de gestão de estoque desenvolvido utilizando **Spring Boot**, com foco em controle eficiente de inventário, rastreabilidade de movimentações e suporte a funcionalidades analíticas para apoio à tomada de decisão.

O sistema tem como objetivo fornecer uma solução robusta para gerenciamento de produtos, fornecedores e estoque, além de incorporar funcionalidades inteligentes como previsão de ruptura de estoque e sugestões automatizadas de reposição.

---

# 2. Tecnologias Utilizadas

O sistema foi desenvolvido utilizando as seguintes tecnologias:

* **Java 21**
* **Spring Boot**
* **Spring Data JPA**
* **MySQL**
* **Maven**
* **Lombok**
* **MapStruct**
* **PlantUML**
* **Swagger / OpenAPI**

---

# 3. Funcionalidades do Sistema

## 3.1 Funcionalidades Básicas (MVP)

As funcionalidades essenciais do sistema incluem:

* Cadastro de produtos
* Controle de estoque (entrada e saída)
* Gerenciamento de categorias de produtos
* Cadastro de fornecedores
* Controle de usuários e permissões
* Histórico de movimentações de estoque
* Alertas de estoque mínimo

---

## 3.2 Funcionalidades Intermediárias

O sistema também inclui funcionalidades intermediárias voltadas para melhor controle operacional:

* Controle de produtos por lote e data de validade
* Inventário com ajuste manual de estoque
* Geração de relatórios operacionais:

  * Produtos mais vendidos
  * Produtos com baixa rotatividade
  * Produtos com estoque crítico
* Importação e exportação de dados em formato **CSV**
* Implementação de **soft delete** para produtos inativos

---

## 3.3 Funcionalidades Inteligentes

Como diferencial, o sistema propõe funcionalidades analíticas e automatizadas:

* Previsão de ruptura de estoque
* Sugestão automática de reposição
* Alertas automáticos por e-mail ou WhatsApp
* Classificação **ABC** de produtos
* Previsão de demanda baseada em histórico de movimentações
* Cálculo automático de custo médio de estoque

---

# 4. Arquitetura do Sistema

O sistema segue o padrão **Layered Architecture (Arquitetura em Camadas)**, amplamente utilizado em aplicações baseadas em **Spring Boot**.

Fluxo de comunicação entre camadas:

```
Client
  ↓
Controller (API REST)
  ↓
Service (Camada de Regras de Negócio)
  ↓
Repository (Camada de Persistência)
  ↓
Database
```

Estrutura de pacotes do projeto:

```
com.example.stock
│
├── controller
├── service
├── repository
├── entity
├── dto
├── mapper
├── enums
├── exception
└── config
```

Essa organização promove **separação de responsabilidades**, facilitando manutenção, testes e evolução do sistema.

---

# 5. Diagramas UML

Para documentação da arquitetura e do domínio do sistema, foram elaborados diversos **diagramas UML utilizando PlantUML**.

Esses diagramas auxiliam na compreensão da estrutura, comportamento e implantação da aplicação.

---

# 5.1 Diagrama de Casos de Uso

O diagrama de casos de uso apresenta as interações entre os usuários e o sistema.

Principais casos de uso identificados:

* Cadastrar produto
* Registrar entrada de estoque
* Registrar saída de estoque
* Gerar relatórios
* Realizar inventário
* Receber alertas de estoque

<img width="393" height="1449" alt="TP51pjCm48NtFeMNPD4xj8I0kgiImNuKJyC8nnF6uug2E0nYmOeLuWIv64vQf2H_kqhizxqPlUTzL11DlJSUpsgLhL3tKQqZmLQ9Wp7GAeizk9x2_R_g1yzNnFknkKR5tfZ90Q7pQ1lbzjFjsdupPlRQtSw-Ai51L06n9s6Nb8k5M3dInyenWD9beJsUMO9Ynq9GF9JViWHiqR6i_EzGC" src="https://github.com/user-attachments/assets/383918f9-fb92-414e-945b-4c292b2bf3e3" />

---

# 5.2 Diagrama de Classes

O diagrama de classes representa o **modelo de domínio da aplicação**, demonstrando as entidades principais e seus relacionamentos.

Principais classes do domínio:

* Product
* Category
* Supplier
* Stock
* StockMovement
* InventoryAdjustment
* ReorderSuggestion
* StockAlert
* PurchaseOrder
* User
* Role

<img width="1634" height="545" alt="bLDDRnCn4BtxLrWv8lN0DQTk2OSWIajsW-0uigTRKtyizZWeGlntxA2DMST4wicQUM_cUJxcDX1uYaOp9ZI4m1-zar4G_yMuacl-qTcHSGi6rxmdhx8LNc8n9gz4mkxKk46X36Z6ZRBAHDEJ4ozhlhM48th6GP0wPAfp6i6otxFaEqWCvu_ND2K6uTL4ojdPMHBqSPgqGdyj0HfGkfZJi" src="https://github.com/user-attachments/assets/a3bda1c6-f760-4f8a-bb2c-563ad60aee77" />

---

# 5.3 Diagrama Entidade-Relacionamento (ER)

O diagrama entidade-relacionamento descreve a **estrutura lógica do banco de dados** utilizado pela aplicação.

Principais relacionamentos identificados:

* Product → Category
* Product → Supplier
* Product → Stock
* Stock → StockMovement
* User → Role

<img width="1435" height="578" alt="fLJDRjim3BxxANmlyW8xrSk2BK3J56csgy5PRAfLiZoHoX0iUVT9iQTKLhCbwCMmFlxmqoUIjyHWsMbL5DYov5tvO8rm3PU_YxAyaQByK7wSVvxVh_rnCl6V5ZHwy5krcdwfLXwW5vUSEokRuF5fDfqlgZkFQDbAxNHDR9eNR-cpGSDoYo7_Sdatg-uzr03ZnjXTdTObrtLAeatWGsGy7" src="https://github.com/user-attachments/assets/e95f9953-21a8-4f6f-b320-ee7e05ba757e" />

---

# 5.4 Diagrama de Arquitetura

O diagrama de arquitetura apresenta a organização do sistema em camadas.

```
Frontend
   ↓
Controllers
   ↓
Services
   ↓
Repositories
   ↓
Database
```

<img width="3105" height="496" alt="arqui" src="https://github.com/user-attachments/assets/f2840fba-df17-461d-a69c-a77bde30ee0c" />

---

# 5.5 Diagrama de Implantação (Deployment)

O diagrama de implantação descreve a infraestrutura necessária para execução do sistema.

```
Client (Browser)
      ↓
Application Server (Spring Boot API)
      ↓
Database Server (MySQL)
      ↓
External Services (Email / WhatsApp API)
```

<img width="1193" height="501" alt="VP1DJiCm48NtFiN8-ueReBQqYqg0W0XrFKs6Oidn1C-47w4U1h7eGN8n72KiWYDscNmlxxsyfIX6RMjlZ3hr11k7nuWrGaMmgnkFGR4xTzyC2oYSA0reAyelBHaJE5tsKThkApgsy6a0IguR3XGKx3hocr242zZJ0LPDOytfzwE2ugkhE2Q_HBmhXw3UOsfIDD658woP5LPtkr4maLnnq" src="https://github.com/user-attachments/assets/96671b4b-5e37-4c23-9bd8-0105441cc69e" />

---

# 6. Modelo de Domínio

O modelo de domínio do sistema é composto pelas seguintes entidades principais:

* Product
* Category
* Supplier
* Stock
* StockMovement
* InventoryAdjustment
* ReorderSuggestion
* StockAlert
* PurchaseOrder
* User
* Role

Essas entidades representam os principais elementos necessários para o gerenciamento completo do estoque.

---

# 7. Regras de Negócio

O sistema implementa algumas regras de negócio fundamentais para garantir consistência dos dados e confiabilidade das operações:

* O sistema **não permite estoque negativo**
* Toda movimentação gera **registro no histórico**
* Ajustes de estoque exigem **motivo**
* Produtos **inativos não podem realizar movimentações**
* Alertas são gerados automaticamente quando:

```
quantity <= minimumStock
```

---

# 8. Evoluções Futuras

Entre as possíveis evoluções do sistema, destacam-se:

* Implementação de autenticação utilizando **JWT e Spring Security**
* Containerização da aplicação com **Docker Compose**
* Utilização de **Redis para cache**
* Desenvolvimento de **dashboard analítico com gráficos**
* Integração com sistemas **ERP**
* Disponibilização de **API pública para integrações externas**

---

# 9. Execução do Projeto

## 9.1 Clonando o repositório

```
git clone https://github.com/seu-usuario/smart-stock.git
```

---

## 9.2 Criação do banco de dados

Criar um banco de dados no **MySQL** com o nome:

```
smart_stock
```

---

## 9.3 Configuração do arquivo application.properties

```
spring.datasource.url=jdbc:mysql://localhost:3306/smart_stock
spring.datasource.username=root
spring.datasource.password=senha

spring.jpa.hibernate.ddl-auto=update
```

---

## 9.4 Execução da aplicação

Executar o comando:

```
mvn spring-boot:run
```

Após a inicialização, a API estará disponível em:

```
http://localhost:8080
```

---

# 10. Licença

Este projeto foi desenvolvido para **fins educacionais**, com o objetivo de estudo de **arquitetura de software, modelagem UML e desenvolvimento backend com Spring Boot**.
