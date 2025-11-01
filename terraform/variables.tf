variable "subscription_id" {
  description = "Azure Subscription ID"
  type        = string
  default     = "339e2872-26be-4ffb-b15e-e85a3e5e4aed"
}

variable "tenant_id" {
  description = "Azure Tenant ID"
  type        = string
  default     = "d4d13448-4ef9-411c-bc92-9654e9f5a3f5"
}

variable "client_id" {
  description = "Azure Service Principal Client ID"
  type        = string
  sensitive   = true
}

variable "client_secret" {
  description = "Azure Service Principal Client Secret"
  type        = string
  sensitive   = true
}

variable "resource_group_name" {
  description = "Name of the resource group"
  type        = string
  default     = "rg-itsm-dev"
}

variable "location" {
  description = "Azure region"
  type        = string
  default     = "switzerlandnorth"
}

variable "acr_name" {
  description = "Azure Container Registry name"
  type        = string
  default     = "acritsmac742"
}

variable "postgresql_server_name" {
  description = "PostgreSQL server name"
  type        = string
  default     = "psqlitsmac742"
}

variable "postgresql_admin_username" {
  description = "PostgreSQL admin username"
  type        = string
  default     = "itsm_admin"
}

variable "postgresql_admin_password" {
  description = "PostgreSQL admin password"
  type        = string
  sensitive   = true
  default     = "Itsm2025Spring"
}

variable "aks_cluster_name" {
  description = "AKS cluster name"
  type        = string
  default     = "aks-itsm-dev"
}

variable "aks_dns_prefix" {
  description = "AKS DNS prefix"
  type        = string
  default     = "aks-itsm"
}

variable "aks_node_count" {
  description = "Number of nodes in AKS cluster"
  type        = number
  default     = 2
}
