output "resource_group_name" {
  description = "Resource group name"
  value       = azurerm_resource_group.itsm.name
}

output "acr_login_server" {
  description = "ACR login server"
  value       = azurerm_container_registry.itsm.login_server
}

output "acr_admin_username" {
  description = "ACR admin username"
  value       = azurerm_container_registry.itsm.admin_username
  sensitive   = true
}

output "acr_admin_password" {
  description = "ACR admin password"
  value       = azurerm_container_registry.itsm.admin_password
  sensitive   = true
}

output "postgresql_fqdn" {
  description = "PostgreSQL FQDN"
  value       = azurerm_postgresql_flexible_server.itsm.fqdn
}

output "aks_cluster_name" {
  description = "AKS cluster name"
  value       = azurerm_kubernetes_cluster.itsm.name
}

output "aks_kube_config" {
  description = "AKS kubeconfig"
  value       = azurerm_kubernetes_cluster.itsm.kube_config_raw
  sensitive   = true
}
