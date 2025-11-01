terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
  }
}

provider "azurerm" {
  features {}
  
  subscription_id = var.subscription_id
  tenant_id       = var.tenant_id
  client_id       = var.client_id
  client_secret   = var.client_secret
  
  skip_provider_registration = true
}

# Resource Group
resource "azurerm_resource_group" "itsm" {
  name     = var.resource_group_name
  location = var.location
}

# Container Registry
resource "azurerm_container_registry" "itsm" {
  name                = var.acr_name
  resource_group_name = azurerm_resource_group.itsm.name
  location            = azurerm_resource_group.itsm.location
  sku                 = "Basic"
  admin_enabled       = true
}

# PostgreSQL Server
resource "azurerm_postgresql_flexible_server" "itsm" {
  name                   = var.postgresql_server_name
  resource_group_name    = azurerm_resource_group.itsm.name
  location               = azurerm_resource_group.itsm.location
  version                = "13"
  administrator_login    = var.postgresql_admin_username
  administrator_password = var.postgresql_admin_password
  storage_mb             = 32768
  sku_name               = "B_Standard_B1ms"
  
  backup_retention_days = 7
}

# PostgreSQL Firewall Rule - Allow Azure Services
resource "azurerm_postgresql_flexible_server_firewall_rule" "allow_azure" {
  name             = "AllowAzureServices"
  server_id        = azurerm_postgresql_flexible_server.itsm.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "0.0.0.0"
}

# AKS Cluster
resource "azurerm_kubernetes_cluster" "itsm" {
  name                = var.aks_cluster_name
  location            = azurerm_resource_group.itsm.location
  resource_group_name = azurerm_resource_group.itsm.name
  dns_prefix          = var.aks_dns_prefix

  default_node_pool {
    name       = "default"
    node_count = var.aks_node_count
    vm_size    = "Standard_DS2_v2"
  }

  identity {
    type = "SystemAssigned"
  }

  network_profile {
    network_plugin = "azure"
    network_policy = "azure"
  }
}

# Role assignment for AKS to pull from ACR
resource "azurerm_role_assignment" "aks_acr_pull" {
  principal_id                     = azurerm_kubernetes_cluster.itsm.kubelet_identity[0].object_id
  role_definition_name             = "AcrPull"
  scope                            = azurerm_container_registry.itsm.id
  skip_service_principal_aad_check = true
}
