import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import * as basketApi from '../api/basketApi.js'

// Keeps the customer's basket for the whole app.
// There are no user accounts, so the browser remembers the basketId in localStorage.

const BasketContext = createContext(null)
const STORAGE_KEY = 'cakeDelightBasketId'

// True when the error means "this saved basket is no longer usable"
// (deleted, or already checked out), so a new basket must be created.
function isStaleBasketError(error) {
  return (error.status === 404 || error.status === 400) && /basket/i.test(error.message)
}

export function BasketProvider({ children }) {
  const [basket, setBasket] = useState(null) // { basketId, status, items, totalAmount }
  const [loading, setLoading] = useState(true)

  // When the app starts, load the basket saved in the browser (if there is one).
  useEffect(() => {
    const savedId = localStorage.getItem(STORAGE_KEY)
    if (!savedId) {
      setLoading(false)
      return
    }

    basketApi
      .getBasket(savedId)
      .then((data) => {
        if (data.status === 'ACTIVE') {
          setBasket(data)
        } else {
          localStorage.removeItem(STORAGE_KEY) // already checked out
        }
      })
      .catch((error) => {
        if (error.status === 404) {
          localStorage.removeItem(STORAGE_KEY)
        }
      })
      .finally(() => setLoading(false))
  }, [])

  async function createNewBasket() {
    const created = await basketApi.createBasket()
    localStorage.setItem(STORAGE_KEY, String(created.basketId))
    return created.basketId
  }

  const addToBasket = useCallback(
    async (cakeId, quantity = 1) => {
      let basketId = basket?.basketId || localStorage.getItem(STORAGE_KEY)
      if (!basketId) {
        basketId = await createNewBasket()
      }

      try {
        setBasket(await basketApi.addItem(basketId, cakeId, quantity))
      } catch (error) {
        if (!isStaleBasketError(error)) {
          throw error
        }
        // The saved basket is gone or finished: start a new one and try once more.
        basketId = await createNewBasket()
        setBasket(await basketApi.addItem(basketId, cakeId, quantity))
      }
    },
    [basket],
  )

  const updateQuantity = useCallback(
    async (itemId, quantity) => {
      setBasket(await basketApi.updateItem(basket.basketId, itemId, quantity))
    },
    [basket],
  )

  const removeItem = useCallback(
    async (itemId) => {
      await basketApi.removeItem(basket.basketId, itemId) // 204: no body
      setBasket(await basketApi.getBasket(basket.basketId))
    },
    [basket],
  )

  // Called after a successful checkout: the basket is finished.
  const clearBasket = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY)
    setBasket(null)
  }, [])

  const itemCount = useMemo(
    () => (basket?.items || []).reduce((sum, item) => sum + item.quantity, 0),
    [basket],
  )

  const value = { basket, loading, itemCount, addToBasket, updateQuantity, removeItem, clearBasket }

  return <BasketContext.Provider value={value}>{children}</BasketContext.Provider>
}

export function useBasket() {
  const context = useContext(BasketContext)
  if (!context) {
    throw new Error('useBasket must be used inside <BasketProvider>')
  }
  return context
}
