"use client";

import { useState, useEffect } from "react";
import "./index.css";

const MainPage = () => {
  const [formData, setFormData] = useState({
    cardNumber: "",
    expiryDate: "",
    cvv: "",
    cardName: "MSCD",
    transactionAmount: "",
    interesDiferido: false,
    cuotas: ""
  });

  const [errors, setErrors] = useState({});
  const [notification, setNotification] = useState({ show: false, message: "", type: "" });
  const [currentTransaction, setCurrentTransaction] = useState(null);
  const [pollingInterval, setPollingInterval] = useState(null);

  // Función para consultar el estado de la transacción
  const checkTransactionStatus = async (transactionId) => {
    try {
      const response = await fetch(`http://localhost:8081/v1/transacciones/${transactionId}/estado`);
      const result = await response.json();
      
      if (result.estado === "AUT") {
        setNotification({
          show: true,
          message: "Transacción autorizada",
          type: "success"
        });
        clearInterval(pollingInterval);
        setCurrentTransaction(null);
      } else if (result.estado === "REC") {
        setNotification({
          show: true,
          message: "Transacción rechazada",
          type: "error"
        });
        clearInterval(pollingInterval);
        setCurrentTransaction(null);
      }
    } catch (error) {
      console.error("Error al consultar estado:", error);
    }
  };

  // Efecto para iniciar el polling cuando hay una transacción en curso
  useEffect(() => {
    if (currentTransaction) {
      const interval = setInterval(() => {
        checkTransactionStatus(currentTransaction.codigoUnicoTransaccion);
      }, 2000); // Consultar cada 2 segundos
      
      setPollingInterval(interval);
      
      return () => clearInterval(interval);
    }
  }, [currentTransaction]);

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;

    if (name === "cvv" && value.length > 3) return;

    if (name === "cardNumber") {
      const formattedValue = value
        .replace(/\D/g, "")
        .replace(/(\d{4})(?=\d)/g, "$1 ");
      setFormData({ ...formData, [name]: formattedValue });
      return;
    }

    if (type === "radio") {
      setFormData({
        ...formData,
        [name]: checked,
        cuotas: !checked ? "" : formData.cuotas
      });
    } else {
      setFormData({
        ...formData,
        [name]: value,
      });
    }

    setErrors({
      ...errors,
      [name]: "",
    });
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
  
    const newErrors = {};
    const cleanCardNumber = formData.cardNumber.replace(/\s/g, "");
    if (!cleanCardNumber || !/^\d{16}$/.test(cleanCardNumber)) {
      newErrors.cardNumber = "El número de la tarjeta debe tener 16 dígitos.";
    }
    if (!formData.expiryDate || !/^(0[1-9]|1[0-2])\/\d{2}$/.test(formData.expiryDate)) {
      newErrors.expiryDate = "La fecha de vencimiento debe estar en formato MM/YY.";
    }
    if (!formData.cvv || formData.cvv.length !== 3) {
      newErrors.cvv = "El CVV debe tener exactamente 3 dígitos.";
    }
    if (!formData.transactionAmount || isNaN(formData.transactionAmount)) {
      newErrors.transactionAmount = "El monto debe ser un número válido.";
    }
    if (formData.interesDiferido && !formData.cuotas) {
      newErrors.cuotas = "Debe seleccionar el número de cuotas para el pago diferido.";
    }
  
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }
  
    try {
      // Preparar datos sensibles para encriptar
      const datosSensibles = JSON.stringify({
        cardNumber: formData.cardNumber.replace(/\s/g, ""),
        expiryDate: formData.expiryDate,
        cvv: formData.cvv,
        nombreTarjeta: "JUAN PEREZ",
        direccionTarjeta: "Av. Principal 123"
      });

      // Enviar transacción con datos encriptados y datos de diferido
      const transactionPayload = {
        monto: parseFloat(formData.transactionAmount),
        marca: formData.cardName,
        datosTarjeta: datosSensibles,
        interesDiferido: formData.interesDiferido,
        cuotas: formData.interesDiferido && formData.cuotas ? parseInt(formData.cuotas) : null
      };

      console.log("Payload a enviar:", transactionPayload);

      const response = await fetch("http://localhost:8081/v1/pagos/procesar", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(transactionPayload),
      });

      const result = await response.json();

      if (!response.ok) {
        setNotification({
          show: true,
          message: result.mensaje || "Error al procesar los datos en el backend",
          type: "error"
        });
        resetForm();
        return;
      }

      // Guardar la transacción actual y mostrar mensaje de procesamiento
      if (result.transaccion) {
        setCurrentTransaction(result.transaccion);
        setNotification({
          show: true,
          message: "Transacción registrada, procesando pago...",
          type: "warning"
        });
      }
      
      resetForm();
    } catch (error) {
      console.error('Error detallado:', error);
      setNotification({
        show: true,
        message: `Error: ${error.message}`,
        type: "error"
      });
      resetForm();
    }
  };

  // Agregar el componente de notificación
  const Notification = ({ message, type }) => (
    <div className={`notification ${type}`} style={{
      position: 'fixed',
      top: '20px',
      right: '20px',
      padding: '15px 25px',
      borderRadius: '5px',
      backgroundColor: type === 'success' ? '#4CAF50' : type === 'error' ? '#f44336' : '#ff9800',
      color: 'white',
      boxShadow: '0 2px 5px rgba(0,0,0,0.2)',
      zIndex: 1000,
      animation: 'slideIn 0.5s ease-out'
    }}>
      {message}
    </div>
  );

  const resetForm = () => {
    setFormData({
      cardNumber: "",
      expiryDate: "",
      cvv: "",
      cardName: "MSCD",
      transactionAmount: "",
      interesDiferido: false,
      cuotas: ""
    });
  };

  return (
    <main className="main-container">
      <h1 className="main-title">Realizar Transacción</h1>
      {notification.show && (
        <Notification 
          message={notification.message} 
          type={notification.type} 
        />
      )}
      <form onSubmit={handleFormSubmit} className="form">
        <div className="form-group">
          <label htmlFor="cardNumber">Número de la Tarjeta</label>
          <input
            type="text"
            id="cardNumber"
            name="cardNumber"
            placeholder="1234 5678 9012 3456"
            value={formData.cardNumber}
            onChange={handleInputChange}
            className="form-input"
          />
          {errors.cardNumber && <p className="error-message">{errors.cardNumber}</p>}
        </div>
        <div className="form-group">
          <label htmlFor="expiryDate">Fecha de Vencimiento</label>
          <input
            type="text"
            id="expiryDate"
            name="expiryDate"
            placeholder="MM/YY"
            value={formData.expiryDate}
            onChange={handleInputChange}
            className="form-input"
          />
          {errors.expiryDate && <p className="error-message">{errors.expiryDate}</p>}
        </div>
        <div className="form-group">
          <label htmlFor="cvv">CVV</label>
          <input
            type="text"
            id="cvv"
            name="cvv"
            placeholder="123"
            value={formData.cvv}
            onChange={handleInputChange}
            className="form-input"
          />
          {errors.cvv && <p className="error-message">{errors.cvv}</p>}
        </div>
        <div className="form-group">
          <label htmlFor="cardName">Tipo de Tarjeta</label>
          <select
            id="cardName"
            name="cardName"
            value={formData.cardName}
            onChange={handleInputChange}
            className="form-input"
          >
            <option value="MSCD">MasterCard</option>
            <option value="VISA">Visa</option>
            <option value="AMEX">American Express</option>
            <option value="DINE">Diners Club</option>
          </select>
        </div>
        <div className="form-group">
          <label htmlFor="transactionAmount">Monto de la Transacción</label>
          <input
            type="text"
            id="transactionAmount"
            name="transactionAmount"
            placeholder="100.50"
            value={formData.transactionAmount}
            onChange={handleInputChange}
            className="form-input"
          />
          {errors.transactionAmount && <p className="error-message">{errors.transactionAmount}</p>}
        </div>

        <div className="form-group">
          <label className="radio-group-label">¿Pago diferido?</label>
          <div className="radio-group">
            <label className="radio-label">
              <input
                type="radio"
                name="interesDiferido"
                value="true"
                checked={formData.interesDiferido === true}
                onChange={(e) => {
                  handleInputChange({
                    target: {
                      name: 'interesDiferido',
                      value: true
                    }
                  });
                }}
                className="form-radio"
              />
              Sí
            </label>
            <label className="radio-label">
              <input
                type="radio"
                name="interesDiferido"
                value="false"
                checked={formData.interesDiferido === false}
                onChange={(e) => {
                  handleInputChange({
                    target: {
                      name: 'interesDiferido',
                      value: false
                    }
                  });
                }}
                className="form-radio"
              />
              No
            </label>
          </div>
        </div>

        {formData.interesDiferido === true && (
          <div className="form-group">
            <label htmlFor="cuotas">Número de Cuotas</label>
            <select
              id="cuotas"
              name="cuotas"
              value={formData.cuotas}
              onChange={handleInputChange}
              className="form-input"
            >
              <option value="">Seleccione el número de cuotas</option>
              <option value="3">3 meses</option>
              <option value="6">6 meses</option>
              <option value="9">9 meses</option>
              <option value="12">12 meses</option>
            </select>
            {errors.cuotas && <p className="error-message">{errors.cuotas}</p>}
          </div>
        )}

        <button type="submit" className="form-button">
          Realizar Transacción
        </button>
      </form>
    </main>
  );
};

export default MainPage;