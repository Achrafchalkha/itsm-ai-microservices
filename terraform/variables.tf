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
